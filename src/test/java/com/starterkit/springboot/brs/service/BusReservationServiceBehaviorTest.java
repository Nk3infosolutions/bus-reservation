package com.starterkit.springboot.brs.service;

import com.starterkit.springboot.brs.dto.model.bus.AgencyDto;
import com.starterkit.springboot.brs.dto.model.bus.StopDto;
import com.starterkit.springboot.brs.dto.model.bus.TripDto;
import com.starterkit.springboot.brs.dto.model.bus.TripScheduleDto;
import com.starterkit.springboot.brs.dto.model.user.UserDto;
import com.starterkit.springboot.brs.model.bus.Agency;
import com.starterkit.springboot.brs.model.bus.Bus;
import com.starterkit.springboot.brs.model.bus.Stop;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BusReservationServiceBehaviorTest {

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

    @Test
    public void getAllStopsShouldMapStopsAndReturnSortedSet() {
        Stop zStop = new Stop().setCode("Z").setName("Zulu");
        Stop aStop = new Stop().setCode("A").setName("Alpha");
        when(stopRepository.findAll()).thenReturn(Arrays.asList(zStop, aStop));
        when(modelMapper.map(zStop, StopDto.class)).thenReturn(new StopDto().setCode("Z").setName("Zulu"));
        when(modelMapper.map(aStop, StopDto.class)).thenReturn(new StopDto().setCode("A").setName("Alpha"));

        java.util.Set<StopDto> stops = busReservationService.getAllStops();

        assertEquals(2, stops.size());
        assertEquals("A", stops.iterator().next().getCode());
    }

    @Test
    public void addAgencyShouldSaveNewAgencyForExistingAdmin() {
        User admin = new User()
                .setEmail("admin@example.com")
                .setRoles(new HashSet<>(Collections.singletonList(new Role().setRole(UserRoles.ADMIN))));
        AgencyDto request = new AgencyDto()
                .setName("City Travel")
                .setDetails("Intercity buses")
                .setOwner(new UserDto().setEmail(admin.getEmail()));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(admin);
        when(agencyRepository.findByName("City Travel")).thenReturn(null);
        when(agencyRepository.save(any(Agency.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(Agency.class), eq(AgencyDto.class))).thenAnswer(invocation -> {
            Agency agency = invocation.getArgument(0);
            return new AgencyDto().setCode(agency.getCode()).setName(agency.getName()).setDetails(agency.getDetails());
        });

        AgencyDto result = busReservationService.addAgency(request);

        assertEquals("City Travel", result.getName());
        assertEquals("Intercity buses", result.getDetails());
        assertNotNull(result.getCode());
        verify(agencyRepository).save(any(Agency.class));
    }

    @Test
    public void getAvailableTripsBetweenStopsShouldReturnMatchingTrips() {
        Stop source = new Stop().setCode("DEL").setName("Delhi");
        Stop destination = new Stop().setCode("MUM").setName("Mumbai");
        Agency agency = new Agency().setCode("AG1");
        Bus bus = new Bus().setCode("BUS1");
        Trip trip = new Trip().setId(21L).setFare(750).setJourneyTime(5)
                .setSourceStop(source).setDestStop(destination).setAgency(agency).setBus(bus);
        when(stopRepository.findByCode("DEL")).thenReturn(source);
        when(stopRepository.findByCode("MUM")).thenReturn(destination);
        when(tripRepository.findAllBySourceStopAndDestStop(source, destination))
                .thenReturn(Collections.singletonList(trip));

        List<TripDto> trips = busReservationService.getAvailableTripsBetweenStops("DEL", "MUM");

        assertEquals(1, trips.size());
        assertEquals("DEL", trips.get(0).getSourceStopCode());
        assertEquals("MUM", trips.get(0).getDestinationStopCode());
        assertEquals("BUS1", trips.get(0).getBusCode());
        assertEquals(750, trips.get(0).getFare());
    }

    @Test
    public void getAvailableTripSchedulesShouldReturnExistingSchedule() {
        Stop source = new Stop().setCode("DEL").setName("Delhi");
        Stop destination = new Stop().setCode("MUM").setName("Mumbai");
        Agency agency = new Agency().setCode("AG1");
        Bus bus = new Bus().setCode("BUS1").setCapacity(36);
        Trip trip = new Trip().setId(31L).setFare(900).setJourneyTime(6)
                .setSourceStop(source).setDestStop(destination).setAgency(agency).setBus(bus);
        TripSchedule schedule = new TripSchedule().setId(41L).setTripDetail(trip).setTripDate("2026-10-20")
                .setAvailableSeats(12);
        when(stopRepository.findByCode("DEL")).thenReturn(source);
        when(stopRepository.findByCode("MUM")).thenReturn(destination);
        when(tripRepository.findAllBySourceStopAndDestStop(source, destination))
                .thenReturn(Collections.singletonList(trip));
        when(tripRepository.findById(31L)).thenReturn(Optional.of(trip));
        when(tripScheduleRepository.findByTripDetailAndTripDate(trip, "2026-10-20")).thenReturn(schedule);

        List<TripScheduleDto> schedules =
                busReservationService.getAvailableTripSchedules("DEL", "MUM", "2026-10-20");

        assertEquals(1, schedules.size());
        assertEquals(Long.valueOf(41L), schedules.get(0).getId());
        assertEquals(12, schedules.get(0).getAvailableSeats());
        assertEquals("BUS1", schedules.get(0).getBusCode());
        verify(tripScheduleRepository).findByTripDetailAndTripDate(trip, "2026-10-20");
    }

    @Test
    public void getAgencyTripsShouldMapAllTripsForAgency() {
        Agency agency = new Agency().setCode("AG1");
        Stop source = new Stop().setCode("DEL").setName("Delhi");
        Stop destination = new Stop().setCode("MUM").setName("Mumbai");
        Bus bus = new Bus().setCode("BUS1");
        Trip trip = new Trip().setId(51L).setFare(500).setJourneyTime(4)
                .setAgency(agency).setSourceStop(source).setDestStop(destination).setBus(bus);
        when(agencyRepository.findByCode("AG1")).thenReturn(agency);
        when(tripRepository.findByAgency(agency)).thenReturn(Collections.singletonList(trip));

        List<TripDto> trips = busReservationService.getAgencyTrips("AG1");

        assertEquals(1, trips.size());
        assertEquals(Long.valueOf(51L), trips.get(0).getId());
        assertEquals("AG1", trips.get(0).getAgencyCode());
    }
}
