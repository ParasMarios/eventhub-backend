package com.paraske.EventHub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.*;
import com.paraske.EventHub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTests {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EventImageRepository eventImageRepository;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GeocodingService geocodingService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private User testUser;
    private Category testCategory;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Music");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Athens");
        testEvent.setDateTime(LocalDateTime.now().plusDays(1));
        testEvent.setOrganizer(testUser);
        testEvent.setCategory(testCategory);
        testEvent.setParticipants(new HashSet<>());

        testReview = new Review();
        testReview.setId(1L);
        testReview.setOverallRating(4);
        testReview.setEvent(testEvent);
    }

    @Test
    void shouldReturnAllEvents() {
        List<Event> events = List.of(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        verify(eventRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsExist() {
        when(eventRepository.findAll()).thenReturn(new ArrayList<>());

        List<Event> result = eventService.getAllEvents();

        assertTrue(result.isEmpty());
        verify(eventRepository).findAll();
    }

    @Test
    void shouldCreateEventWithValidOrganizer() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.createEvent(testEvent);

        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        assertEquals(testUser.getId(), result.getOrganizer().getId());
        verify(userRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldThrowExceptionWhenOrganizerNotFoundDuringCreation() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.createEvent(testEvent));
        verify(userRepository).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldCreateEventWithTickets() {
        List<Ticket> tickets = new ArrayList<>();
        Ticket ticket = new Ticket();
        ticket.setType("VIP");
        ticket.setPrice(BigDecimal.valueOf(50));
        tickets.add(ticket);
        testEvent.setTickets(tickets);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.createEvent(testEvent);

        assertEquals(1, result.getTickets().size());
        assertEquals(testEvent, result.getTickets().get(0).getEvent());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldCreateEventWithoutCategory() {
        testEvent.setCategory(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.createEvent(testEvent);

        assertNotNull(result);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldUpdateEventByOrganizer() {
        Event updatedDetails = new Event();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setLocation("Thessaloniki");
        updatedDetails.setDateTime(LocalDateTime.now().plusDays(2));
        updatedDetails.setEndDateTime(LocalDateTime.now().plusDays(2).plusHours(2));
        updatedDetails.setBookingDescription("Updated Booking");
        updatedDetails.setBookingUrl("http://booking.url");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.updateEvent(1L, updatedDetails, "testuser");

        assertEquals("Updated Title", testEvent.getTitle());
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldThrowAccessDeniedWhenUpdatingEventAsNonOrganizer() {
        Event updatedDetails = new Event();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(AccessDeniedException.class,
                () -> eventService.updateEvent(1L, updatedDetails, "differentuser"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void shouldUpdateEventCategory() {
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Sports");

        Event updatedDetails = new Event();
        updatedDetails.setTitle("Test Event");
        updatedDetails.setCategory(newCategory);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        eventService.updateEvent(1L, updatedDetails, "testuser");

        verify(categoryRepository).findById(2L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldUpdateEventTickets() {
        List<Ticket> newTickets = new ArrayList<>();
        Ticket newTicket = new Ticket();
        newTicket.setType("Regular");
        newTicket.setPrice(BigDecimal.valueOf(20));
        newTickets.add(newTicket);

        Event updatedDetails = new Event();
        updatedDetails.setTitle("Test Event");
        updatedDetails.setTickets(newTickets);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        eventService.updateEvent(1L, updatedDetails, "testuser");

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldJoinUserToEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        eventService.joinEvent(1L, "testuser");

        assertTrue(testEvent.getParticipants().contains(testUser));
        verify(eventRepository).save(testEvent);
    }

    @Test
    void shouldNotDuplicateUserWhenJoiningEvent() {
        testEvent.getParticipants().add(testUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        eventService.joinEvent(1L, "testuser");

        assertEquals(1, testEvent.getParticipants().size());
        verify(eventRepository).save(testEvent);
    }

    @Test
    void shouldThrowExceptionWhenEventNotFoundForJoin() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eventService.joinEvent(1L, "testuser"));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForJoin() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eventService.joinEvent(1L, "testuser"));
    }

    @Test
    void shouldGetJoinedEventsByUser() {
        Set<Event> joinedEvents = new HashSet<>();
        joinedEvents.add(testEvent);
        testUser.setJoinedEvents(joinedEvents);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Set<Event> result = eventService.getJoinedEventsByUser(1L);

        assertEquals(1, result.size());
        assertTrue(result.contains(testEvent));
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetEmptyJoinedEventsWhenUserHasNone() {
        testUser.setJoinedEvents(new HashSet<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Set<Event> result = eventService.getJoinedEventsByUser(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetEventStatsWithReviews() {
        List<Review> reviews = List.of(testReview);
        when(reviewRepository.findByEventId(1L)).thenReturn(reviews);

        EventRatingStats result = eventService.getEventStats(1L);

        assertEquals(4.0, result.getAverageOverall());
        assertEquals(1L, result.getTotalReviews());
    }

    @Test
    void shouldCalculateAverageRatingFromMultipleReviews() {
        Review review2 = new Review();
        review2.setOverallRating(5);
        List<Review> reviews = List.of(testReview, review2);
        when(reviewRepository.findByEventId(1L)).thenReturn(reviews);

        EventRatingStats result = eventService.getEventStats(1L);

        assertEquals(4.5, result.getAverageOverall());
        assertEquals(2L, result.getTotalReviews());
    }

    @Test
    void shouldReturnZeroStatsWhenNoReviews() {
        when(reviewRepository.findByEventId(1L)).thenReturn(new ArrayList<>());

        EventRatingStats result = eventService.getEventStats(1L);

        assertEquals(0.0, result.getAverageOverall());
        assertEquals(0L, result.getTotalReviews());
    }

    @Test
    void shouldFilterEventsByTitle() {
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq("Test"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents("Test", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }

    @Test
    void shouldFilterEventsByCategory() {
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents(null, null, null, null, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterEventsByCategoryWithParent() {
        Event eventWithChildCategory = new Event();
        eventWithChildCategory.setId(2L);
        Category childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setParent(testCategory);
        eventWithChildCategory.setCategory(childCategory);

        List<Event> allEvents = List.of(testEvent, eventWithChildCategory);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents(null, null, null, null, 1L);

        assertEquals(2, result.size());
    }

    @Test
    void shouldExcludeEventsWithWrongCategory() {
        Event eventWithOtherCategory = new Event();
        eventWithOtherCategory.setId(2L);
        Category otherCategory = new Category();
        otherCategory.setId(99L);
        eventWithOtherCategory.setCategory(otherCategory);

        List<Event> allEvents = List.of(testEvent, eventWithOtherCategory);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents(null, null, null, null, 1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldFilterEventsByLocationWithinRadius() {
        testEvent.setLatitude(37.9838);
        testEvent.setLongitude(23.7275);
        List<Event> allEvents = List.of(testEvent);

        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(new double[]{37.9838, 23.7275});

        List<Event> result = eventService.filterEvents(null, "Athens", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldExpandRadiusWhenFewerThan5ResultsFound() {
        Event nearEvent = new Event();
        nearEvent.setId(2L);
        nearEvent.setLatitude(37.9838);
        nearEvent.setLongitude(23.7275);

        Event farEvent = new Event();
        farEvent.setId(3L);
        farEvent.setLatitude(38.5);
        farEvent.setLongitude(24.0);

        testEvent.setLatitude(37.9838);
        testEvent.setLongitude(23.7275);

        List<Event> allEvents = List.of(testEvent, nearEvent, farEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(new double[]{37.9838, 23.7275});

        List<Event> result = eventService.filterEvents(null, "Athens", null, null, null);

        assertNotNull(result);
        verify(geocodingService).getCoordinates("Athens");
    }

    @Test
    void shouldFilterByLocationTextWhenGeocodingFails() {
        testEvent.setLocation("Athens Greece");
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(null);

        List<Event> result = eventService.filterEvents(null, "Athens", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterByLocationTextWhenCoordinatesNull() {
        testEvent.setLocation("Athens Greece");
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(null);

        List<Event> result = eventService.filterEvents(null, "Athens", null, null, null);

        assertTrue(result.stream().anyMatch(e -> e.getLocation().contains("Athens")));
    }

    @Test
    void shouldHandleAccentedCharactersInLocationFilter() {
        testEvent.setLocation("Athína Greece");
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athína")).thenReturn(null);

        List<Event> result = eventService.filterEvents(null, "Athína", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldUseDefaultDateRangeWhenNotProvided() {
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq("Test"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents("Test", null, null, null, null);

        assertEquals(1, result.size());
        verify(eventRepository).findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq("Test"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void shouldIdentifyOrganizerCorrectly() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        boolean result = eventService.isOrganizer(1L, 1L);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForNonOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        boolean result = eventService.isOrganizer(1L, 999L);

        assertFalse(result);
    }

    @Test
    void shouldGetEventById() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Optional<Event> result = eventService.getEventById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getTitle());
    }

    @Test
    void shouldReturnEmptyOptionalWhenEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Event> result = eventService.getEventById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldGetEventsByOrganizer() {
        List<Event> organizerEvents = List.of(testEvent);
        when(eventRepository.findByOrganizerId(1L)).thenReturn(organizerEvents);

        List<Event> result = eventService.getEventsByOrganizer(1L);

        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyListWhenOrganizerHasNoEvents() {
        when(eventRepository.findByOrganizerId(1L)).thenReturn(new ArrayList<>());

        List<Event> result = eventService.getEventsByOrganizer(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteEventAsOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        eventService.secureDeleteEvent(1L, "testuser");

        verify(eventRepository).deleteById(1L);
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingAsNonOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(AccessDeniedException.class,
                () -> eventService.secureDeleteEvent(1L, "differentuser"));
        verify(eventRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.secureDeleteEvent(1L, "testuser"));
    }

    @Test
    void shouldUploadGalleryImages() throws IOException {
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/image.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("image bytes".getBytes());

        EventImage savedImage = new EventImage("https://res.cloudinary.com/image.jpg", testEvent, testUser);
        when(eventImageRepository.save(any(EventImage.class))).thenReturn(savedImage);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        List<EventImage> result = eventService.uploadGalleryImages(1L, new MultipartFile[]{file}, testUser);

        assertEquals(1, result.size());
        assertEquals("https://res.cloudinary.com/image.jpg", result.get(0).getImageUrl());
        verify(uploader).upload(any(byte[].class), anyMap());
        verify(eventImageRepository).save(any(EventImage.class));
    }

    @Test
    void shouldHandleMultipleImagesUpload() throws IOException {
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/image.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getBytes()).thenReturn("image1 bytes".getBytes());
        when(file2.getBytes()).thenReturn("image2 bytes".getBytes());

        EventImage savedImage = new EventImage("https://res.cloudinary.com/image.jpg", testEvent, testUser);
        when(eventImageRepository.save(any(EventImage.class))).thenReturn(savedImage);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        List<EventImage> result = eventService.uploadGalleryImages(1L, new MultipartFile[]{file1, file2}, testUser);

        assertEquals(2, result.size());
        verify(uploader, times(2)).upload(any(byte[].class), anyMap());
    }

    @Test
    void shouldThrowExceptionWhenEventNotFoundForImageUpload() {
        MultipartFile file = mock(MultipartFile.class);
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.uploadGalleryImages(1L, new MultipartFile[]{file}, testUser));
    }

    @Test
    void shouldThrowExceptionWhenCloudinaryUploadFails() throws IOException {
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn("image bytes".getBytes());
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(RuntimeException.class,
                () -> eventService.uploadGalleryImages(1L, new MultipartFile[]{file}, testUser));
    }

    @Test
    void shouldDeleteGalleryImageByUploader() {
        EventImage image = new EventImage();
        image.setId(1L);
        image.setUploader(testUser);
        image.setEvent(testEvent);

        when(eventImageRepository.findById(1L)).thenReturn(Optional.of(image));

        eventService.deleteGalleryImage(1L, testUser);

        verify(eventImageRepository).delete(image);
    }

    @Test
    void shouldDeleteGalleryImageByOrganizer() {
        User differentUploader = new User();
        differentUploader.setId(999L);

        EventImage image = new EventImage();
        image.setId(1L);
        image.setUploader(differentUploader);
        image.setEvent(testEvent);

        when(eventImageRepository.findById(1L)).thenReturn(Optional.of(image));

        eventService.deleteGalleryImage(1L, testUser);

        verify(eventImageRepository).delete(image);
    }

    @Test
    void shouldThrowExceptionWhenDeletingImageAsUnauthorizedUser() {
        User uploader = new User();
        uploader.setId(2L);

        User differentUser = new User();
        differentUser.setId(3L);

        User eventOrganizer = new User();
        eventOrganizer.setId(4L);

        Event eventWithDifferentOrganizer = new Event();
        eventWithDifferentOrganizer.setOrganizer(eventOrganizer);

        EventImage image = new EventImage();
        image.setId(1L);
        image.setUploader(uploader);
        image.setEvent(eventWithDifferentOrganizer);

        when(eventImageRepository.findById(1L)).thenReturn(Optional.of(image));

        assertThrows(RuntimeException.class,
                () -> eventService.deleteGalleryImage(1L, differentUser));
        verify(eventImageRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenImageNotFound() {
        when(eventImageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.deleteGalleryImage(1L, testUser));
    }

    @Test
    void shouldCalculateHaversineDistanceCorrectly() {
        testEvent.setLatitude(37.9838);
        testEvent.setLongitude(23.7275);
        List<Event> allEvents = List.of(testEvent);

        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(new double[]{37.9838, 23.7275});

        List<Event> result = eventService.filterEvents(null, "Athens", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterOutEventsOutsideRadius() {
        testEvent.setLatitude(37.9838);
        testEvent.setLongitude(23.7275);

        Event farEvent = new Event();
        farEvent.setId(2L);
        farEvent.setTitle("Far Event");
        farEvent.setLatitude(40.0);
        farEvent.setLongitude(25.0);

        List<Event> allEvents = List.of(testEvent, farEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(new double[]{37.9838, 23.7275});

        List<Event> result = eventService.filterEvents(null, "Athens", null, null, null);

        assertTrue(result.stream().noneMatch(e -> e.getId().equals(2L)));
    }

    @Test
    void shouldHandleEventWithoutCoordinates() {
        Event eventWithoutCoordinates = new Event();
        eventWithoutCoordinates.setId(2L);
        eventWithoutCoordinates.setLocation("Location Text");

        List<Event> allEvents = List.of(eventWithoutCoordinates);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Location Text")).thenReturn(null);

        List<Event> result = eventService.filterEvents(null, "Location Text", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldHandleEmptyLocationString() {
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents(null, "", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterEventsByComplexCriteria() {
        testEvent.setLatitude(37.9838);
        testEvent.setLongitude(23.7275);
        List<Event> allEvents = List.of(testEvent);

        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq("Test"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);
        when(geocodingService.getCoordinates("Athens")).thenReturn(new double[]{37.9838, 23.7275});

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        List<Event> result = eventService.filterEvents("Test", "Athens", start, end, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldHandleNullLocationWhenSearching() {
        List<Event> allEvents = List.of(testEvent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq("Test"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents("Test", null, null, null, null);

        assertEquals(1, result.size());
        verify(geocodingService, never()).getCoordinates(anyString());
    }

    @Test
    void shouldHandleEventCategoryChildrenCorrectly() {
        Category parent = new Category();
        parent.setId(1L);

        Category child = new Category();
        child.setId(2L);
        child.setParent(parent);

        Event eventWithChild = new Event();
        eventWithChild.setId(2L);
        eventWithChild.setCategory(child);

        Event eventWithParent = new Event();
        eventWithParent.setId(3L);
        eventWithParent.setCategory(parent);

        List<Event> allEvents = List.of(eventWithChild, eventWithParent);
        when(eventRepository.findByTitleContainingIgnoreCaseAndDateTimeBetween(
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(allEvents);

        List<Event> result = eventService.filterEvents(null, null, null, null, 1L);

        assertEquals(2, result.size());
    }
}

