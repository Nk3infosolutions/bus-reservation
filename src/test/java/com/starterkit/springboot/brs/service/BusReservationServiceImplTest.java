package com.starterkit.springboot.brs.service;

import com.starterkit.springboot.brs.dto.model.bus.AgencyDto;
import com.starterkit.springboot.brs.dto.model.bus.StopDto;
import com.starterkit.springboot.brs.dto.model.bus.TicketDto;
import com.starterkit.springboot.brs.dto.model.bus.TripDto;
import com.starterkit.springboot.brs.dto.model.bus.TripScheduleDto;
import com.starterkit.springboot.brs.dto.model.user.UserDto;
import com.starterkit.springboot.brs.model.bus.Agency;
import com.starterkit.springboot.brs.model.bus.Bus;
import com.starterkit.springboot.brs.model.bus.Stop;
import com.starterkit.springboot.brs.model.bus.Ticket;
import com.starterkit.springboot.brs.model.bus.Trip;
import com.starterkit.springboot.brs.model.bus.TripSchedule;
import com.starterkit.springboot.brs.model.user.Role;
import com.starterkit.springboot.brs.model.user.User;
import com.starterkit.springboot.brs.model.user.UserRoles;
import com.starterkit.springboot.brs.repository.bus.AgencyRepository;
import com.starterkit.springboot.brs.repository.bus.BusRepository;
import com.starterkit.springboot.brs.repository.bus.StopRepository;
import com.starterkit.springboot.brs.repository.bus.TicketRepository;
import com.starterkit.springboot.brs.repository.bus.TripRepository;
import com.starterkit.springboot.brs.repository.bus.TripScheduleRepository;
import com.starterkit.springboot.brs.repository.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BusReservationServiceImplTest {

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripScheduleRepository tripScheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BusReservationServiceImpl busReservationService;

    @Before
    public void setUp() {
        when(modelMapper.map(any(Agency.class), eq(AgencyDto.class))).thenAnswer(invocation -> {
            Agency source = invocation.getArgument(0);
            AgencyDto dto = new AgencyDto();
            dto.setCode(source.getCode());
            dto.setName(source.getName());
            dto.setDetails(source.getDetails());
            return dto;
        });
    }

    @Test
    public void addAgencyShouldCreateAgencyForOwner() {
        User owner = new User().setId(1L).setEmail("admin@example.com").setRoles(new HashSet<>(Collections.singletonList(new Role().setRole(UserRoles.ADMIN))));
        AgencyDto request = new AgencyDto().setName("City Travels").setDetails("Premium travel").setOwner(new UserDto().setEmail("admin@example.com"));

        when(userRepository.findByEmail("admin@example.com")).thenReturn(owner);
        when(agencyRepository.findByName("City Travels")).thenReturn(null);
        when(agencyRepository.save(any(Agency.class))).thenAnswer(invocation -> {
            Agency agency = invocation.getArgument(0);
            agency.setId(7L);
            return agency;
        });

        AgencyDto result = busReservationService.addAgency(request);

        assertNotNull(result);
        assertEquals("City Travels", result.getName());
        assertEquals("Premium travel", result.getDetails());
        verify(agencyRepository).save(any(Agency.class));
    }

    @Test
    public void getAvailableTripSchedulesShouldCreateScheduleWhenMissing() {
        Stop source = new Stop().setCode("DEL").setName("Delhi");
        Stop dest = new Stop().setCode("MUM").setName("Mumbai");
        User owner = new User().setId(11L).setEmail("admin@example.com");
        Agency agency = new Agency().setCode("AG1").setName("TravelCo").setOwner(owner);
        Bus bus = new Bus().setCode("BUS1").setCapacity(30).setMake("Volvo").setAgency(agency);
        Trip trip = new Trip().setId(51L).setSourceStop(source).setDestStop(dest).setAgency(agency).setBus(bus).setFare(1200).setJourneyTime(6);

        when(stopRepository.findByCode("DEL")).thenReturn(source);
        when(stopRepository.findByCode("MUM")).thenReturn(dest);
        when(tripRepository.findAllBySourceStopAndDestStop(source, dest)).thenReturn(Collections.singletonList(trip));
        when(tripRepository.findById(51L)).thenReturn(java.util.Optional.of(trip));
        when(tripScheduleRepository.findByTripDetailAndTripDate(trip, "2024-04-10")).thenReturn(null);
        when(tripScheduleRepository.save(any(TripSchedule.class))).thenAnswer(invocation -> {
            TripSchedule schedule = invocation.getArgument(0);
            schedule.setId(98L);
            schedule.setAvailableSeats(30);
            return schedule;
        });

        List<TripScheduleDto> schedules = busReservationService.getAvailableTripSchedules("DEL", "MUM", "2024-04-10");

        assertNotNull(schedules);
        assertEquals(1, schedules.size());
        assertEquals("BUS1", schedules.get(0).getBusCode());
        assertEquals(30, schedules.get(0).getAvailableSeats());
        verify(tripScheduleRepository).save(any(TripSchedule.class));
    }

    @Test
    public void bookTicketShouldReduceAvailableSeatsAndReturnTicketDto() {
        User passenger = new User().setId(7L).setEmail("user@example.com").setFirstName("User").setLastName("Test").setMobileNumber("7676767676").setRoles(new HashSet<>());
        Stop source = new Stop().setCode("DEL").setName("Delhi");
        Stop dest = new Stop().setCode("MUM").setName("Mumbai");
        Agency agency = new Agency().setCode("AG1").setName("TravelCo");
        Bus bus = new Bus().setCode("BUS1").setCapacity(20).setMake("Mercedes").setAgency(agency);
        Trip trip = new Trip().setId(61L).setSourceStop(source).setDestStop(dest).setAgency(agency).setBus(bus).setFare(1000).setJourneyTime(5);
        TripSchedule tripSchedule = new TripSchedule().setId(90L).setTripDetail(trip).setTripDate("2024-04-15").setAvailableSeats(20);
        UserDto userDto = new UserDto().setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(passenger);
        when(tripScheduleRepository.findById(90L)).thenReturn(java.util.Optional.of(tripSchedule));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(40L);
            ticket.setSeatNumber(0);
            return ticket;
        });
        when(tripScheduleRepository.save(any(TripSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketDto result = busReservationService.bookTicket(new TripScheduleDto().setId(90L).setTripId(61L).setTripDate("2024-04-15"), userDto);

        assertNotNull(result);
        assertEquals("BUS1", result.getBusCode());
        assertEquals("User Test", result.getPassengerName());
        assertEquals("7676767676", result.getPassengerMobileNumber());
        assertEquals(19, tripSchedule.getAvailableSeats());
        verify(ticketRepository).save(any(Ticket.class));
        verify(tripScheduleRepository).save(tripSchedule);
    }
}
