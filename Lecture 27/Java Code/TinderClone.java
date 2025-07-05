import java.util.*;
import java.lang.Math;
import java.text.SimpleDateFormat;

// -------------------- Observer Pattern -------------------- //

// Observer Pattern: Interface for notification observers
interface NotificationObserver {
    void update(String message);
}

// Concrete observer
class UserNotificationObserver implements NotificationObserver {
    private String userId;
    public UserNotificationObserver(String id) {
        userId = id;
    }
    public void update(String message) {
        System.out.println("Notification for user " + userId + ": " + message);
    }
}

// Observable for Observer Pattern
class NotificationService {
    private Map<String, NotificationObserver> observers;

    // Singleton Pattern
    private static NotificationService instance;
    private NotificationService() {
        observers = new HashMap<>();
    }
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    public void registerObserver(String userId, NotificationObserver observer) {
        observers.put(userId, observer);
    }
    public void removeObserver(String userId) {
        observers.remove(userId);
    }
    public void notifyUser(String userId, String message) {
        if (observers.containsKey(userId)) {
            observers.get(userId).update(message);
        }
    }
    public void notifyAll(String message) {
        for (Map.Entry<String, NotificationObserver> pair : observers.entrySet()) {
            pair.getValue().update(message);
        }
    }
}

// -------------------- Basic Models -------------------- //

// Gender enum
enum Gender {
    MALE,
    FEMALE,
    NON_BINARY,
    OTHER
}

// Location class
class Location {
    private double latitude;
    private double longitude;

    public Location() {
        latitude = 0.0;
        longitude = 0.0;
    }

    public Location(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double lat) {
        latitude = lat;
    }

    public void setLongitude(double lon) {
        longitude = lon;
    }

    // Calculate distance in kilometers between two locations using Haversine formula
    public double distanceInKm(Location other) {
        final double earthRadiusKm = 6371.0;
        double dLat = (other.latitude - latitude) * Math.PI / 180.0;
        double dLon = (other.longitude - longitude) * Math.PI / 180.0;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(latitude * Math.PI / 180.0) * Math.cos(other.latitude * Math.PI / 180.0) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadiusKm * c;
    }
}

// Interest class
class Interest {
    private String name;
    private String category;

    public Interest() {
        name = "";
        category = "";
    }

    public Interest(String n, String c) {
        name = n;
        category = c;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}

// Preference class
class Preference {
    private List<Gender> interestedIn;
    private int minAge;
    private int maxAge;
    private double maxDistance; // in kilometers
    private List<String> interests;

    public Preference() {
        interestedIn = new ArrayList<>();
        interests = new ArrayList<>();
        minAge = 18;
        maxAge = 100;
        maxDistance = 100.0;
    }

    public void addGenderPreference(Gender gender) {
        interestedIn.add(gender);
    }

    public void removeGenderPreference(Gender gender) {
        interestedIn.remove(gender);
    }

    public void setAgeRange(int min, int max) {
        minAge = min;
        maxAge = max;
    }

    public void setMaxDistance(double distance) {
        maxDistance = distance;
    }

    public void addInterest(String interest) {
        interests.add(interest);
    }

    public void removeInterest(String interest) {
        interests.remove(interest);
    }

    public boolean isInterestedInGender(Gender gender) {
        return interestedIn.contains(gender);
    }

    public boolean isAgeInRange(int age) {
        return age >= minAge && age <= maxAge;
    }

    public boolean isDistanceAcceptable(double distance) {
        return distance <= maxDistance;
    }

    public List<String> getInterests() {
        return interests;
    }

    public List<Gender> getInterestedGenders() {
        return interestedIn;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public double getMaxDistance() {
        return maxDistance;
    }
}

// -------------------- Message System -------------------- //

// Message class
class Message {
    private String senderId;
    private String content;
    private long timestamp;

    public Message(String sender, String msg) {
        senderId = sender;
        content = msg;
        timestamp = System.currentTimeMillis();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
}

// Chat room class
class ChatRoom {
    private String id;
    private List<String> participantIds;
    private List<Message> messages;

    public ChatRoom(String roomId, String user1Id, String user2Id) {
        id = roomId;
        participantIds = new ArrayList<>();
        participantIds.add(user1Id);
        participantIds.add(user2Id);
        messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void addMessage(String senderId, String content) {
        Message msg = new Message(senderId, content);
        messages.add(msg);
    }

    public boolean hasParticipant(String userId) {
        return participantIds.contains(userId);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<String> getParticipants() {
        return participantIds;
    }

    public void displayChat() {
        System.out.println("===== Chat Room: " + id + " =====");
        for (Message msg : messages) {
            System.out.println("[" + msg.getFormattedTime() + "] " + msg.getSenderId() + ": " + msg.getContent());
        }
        System.out.println("=========================");
    }
}

// -------------------- Profile System -------------------- //

// Profile class
class UserProfile {
    private String name;
    private int age;
    private Gender gender;
    private String bio;
    private List<String> photos;
    private List<Interest> interests;
    private Location location;

    public UserProfile() {
        name = "";
        age = 0;
        gender = Gender.OTHER;
        photos = new ArrayList<>();
        interests = new ArrayList<>();
        location = new Location();
    }

    public void setName(String n) {
        name = n;
    }

    public void setAge(int a) {
        age = a;
    }

    public void setGender(Gender g) {
        gender = g;
    }

    public void setBio(String b) {
        bio = b;
    }

    public void addPhoto(String photoUrl) {
        photos.add(photoUrl);
    }

    public void removePhoto(String photoUrl) {
        photos.remove(photoUrl);
    }

    public void addInterest(String name, String category) {
        Interest interest = new Interest(name, category);
        interests.add(interest);
    }

    public void removeInterest(String name) {
        interests.removeIf(i -> i.getName().equals(name));
    }

    public void setLocation(Location loc) {
        location = loc;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public Location getLocation() {
        return location;
    }

    public void display() {
        System.out.println("===== Profile =====");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.print("Gender: ");
        switch (gender) {
            case MALE:
                System.out.print("Male");
                break;
            case FEMALE:
                System.out.print("Female");
                break;
            case NON_BINARY:
                System.out.print("Non-binary");
                break;
            case OTHER:
                System.out.print("Other");
                break;
        }
        System.out.println();
        System.out.println("Bio: " + bio);
        System.out.print("Photos: ");
        for (String photo : photos) {
            System.out.print(photo + ", ");
        }
        System.out.println();
        System.out.print("Interests: ");
        for (Interest i : interests) {
            System.out.print(i.getName() + " (" + i.getCategory() + "), ");
        }
        System.out.println();
        System.out.println("Location: " + location.getLatitude() + ", " + location.getLongitude());
        System.out.println("===================");
    }
}

// -------------------- User System -------------------- //

// Swipe action enum
enum SwipeAction {
    LEFT,  // Dislike
    RIGHT  // Like
}

// User class
class User {
    private String id;
    private UserProfile profile;
    private Preference preference;
    private Map<String, SwipeAction> swipeHistory;    // userId -> action
    private NotificationObserver notificationObserver;

    public User(String userId) {
        id = userId;
        profile = new UserProfile();
        preference = new Preference();
        swipeHistory = new HashMap<>();
        notificationObserver = new UserNotificationObserver(userId);
        NotificationService.getInstance().registerObserver(userId, notificationObserver);
    }

    public String getId() {
        return id;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public Preference getPreference() {
        return preference;
    }

    public void swipe(String otherUserId, SwipeAction action) {
        swipeHistory.put(otherUserId, action);
    }

    public boolean hasLiked(String otherUserId) {
        return swipeHistory.containsKey(otherUserId) && swipeHistory.get(otherUserId) == SwipeAction.RIGHT;
    }

    public boolean hasDisliked(String otherUserId) {
        return swipeHistory.containsKey(otherUserId) && swipeHistory.get(otherUserId) == SwipeAction.LEFT;
    }

    public boolean hasInteractedWith(String otherUserId) {
        return swipeHistory.containsKey(otherUserId);
    }

    public void displayProfile() {  // Principle of least knowledge
        profile.display();
    }
}

// -------------------- Location Service -------------------- //

// Strategy Pattern: Location service strategy interface
interface LocationStrategy {
    List<User> findNearbyUsers(Location location, double maxDistance, List<User> allUsers);
}

// Concrete strategy: Basic location strategy
class BasicLocationStrategy implements LocationStrategy {
    public List<User> findNearbyUsers(Location location, double maxDistance, List<User> allUsers) {
        List<User> nearbyUsers = new ArrayList<>();
        for (User user : allUsers) {
            double distance = location.distanceInKm(user.getProfile().getLocation());
            if (distance <= maxDistance) {
                nearbyUsers.add(user);
            }
        }
        return nearbyUsers;
    }
}

// Location service with Strategy Pattern
class LocationService {
    private LocationStrategy strategy;

    // Singleton Pattern
    private static LocationService instance;

    private LocationService() {
        strategy = new BasicLocationStrategy();
    }

    public static LocationService getInstance() {
        if (instance == null) {
            instance = new LocationService();
        }
        return instance;
    }

    public void setStrategy(LocationStrategy newStrategy) {
        strategy = newStrategy;
    }

    public List<User> findNearbyUsers(Location location, double maxDistance, List<User> allUsers) {
        return strategy.findNearbyUsers(location, maxDistance, allUsers);
    }
}

// -------------------- Matching System -------------------- //

enum MatcherType {
    BASIC,
    INTERESTS_BASED,
    LOCATION_BASED
}

// Matcher interface
interface Matcher {
    double calculateMatchScore(User user1, User user2);
}

// Concrete matcher: Basic matcher
class BasicMatcher implements Matcher {
    public double calculateMatchScore(User user1, User user2) {
         // Basic scoring, just check if preferences align
        boolean user1LikesUser2Gender = user1.getPreference().isInterestedInGender(user2.getProfile().getGender());
        boolean user2LikesUser1Gender = user2.getPreference().isInterestedInGender(user1.getProfile().getGender());

        if (!user1LikesUser2Gender || !user2LikesUser1Gender) {
            return 0.0;
        }

         // Check age preference
        boolean user1LikesUser2Age = user1.getPreference().isAgeInRange(user2.getProfile().getAge());
        boolean user2LikesUser1Age = user2.getPreference().isAgeInRange(user1.getProfile().getAge());

        if (!user1LikesUser2Age || !user2LikesUser1Age) {
            return 0.0;
        }

        // Check distance preference
        double distance = user1.getProfile().getLocation().distanceInKm(user2.getProfile().getLocation());
        boolean user1LikesUser2Distance = user1.getPreference().isDistanceAcceptable(distance);
        boolean user2LikesUser1Distance = user2.getPreference().isDistanceAcceptable(distance);

        if (!user1LikesUser2Distance || !user2LikesUser1Distance) {
            return 0.0;
        }

        // If all basic criteria match, return a base score
        return 0.5; // 50% match
    }
}

// Concrete matcher: Interests-based matcher
class InterestsBasedMatcher implements Matcher {
    public double calculateMatchScore(User user1, User user2) {
        // First, check basic compatibility
        BasicMatcher basicMatcher = new BasicMatcher();
        double baseScore = basicMatcher.calculateMatchScore(user1, user2);

        if (baseScore == 0.0) {
            return 0.0; // No need to continue if basic criteria don't match
        }

        // Calculate score based on shared interests
        List<String> user1InterestNames = new ArrayList<>();
        for (Interest interest : user1.getProfile().getInterests()) {
            user1InterestNames.add(interest.getName());
        }

        int sharedInterests = 0;
        for (Interest interest : user2.getProfile().getInterests()) {
            if (user1InterestNames.contains(interest.getName())) {
                sharedInterests++;
            }
        }

        // Bonus score based on shared interests (up to 0.5 additional points)
        double maxInterests = Math.max(user1.getProfile().getInterests().size(), user2.getProfile().getInterests().size());
        double interestScore = maxInterests > 0 ? 0.5 * ((double) sharedInterests / maxInterests) : 0.0;

        return baseScore + interestScore;
    }
}

// Concrete matcher: Location-based matcher
class LocationBasedMatcher implements Matcher {
    public double calculateMatchScore(User user1, User user2) {
        // First, check basic compatibility
        InterestsBasedMatcher interestsMatcher = new InterestsBasedMatcher();
        double baseScore = interestsMatcher.calculateMatchScore(user1, user2);

        if (baseScore == 0.0) {
            return 0.0; // No need to continue if basic criteria don't match
        }

        // Calculate score based on proximity
        double distance = user1.getProfile().getLocation().distanceInKm(user2.getProfile().getLocation());
        double maxDistance = Math.min(user1.getPreference().getMaxDistance(), user2.getPreference().getMaxDistance());

        // Closer is better, score decreases with distance (up to 0.2 additional points)
        double proximityScore = maxDistance > 0 ? 0.2 * (1.0 - (distance / maxDistance)) : 0.0;

        return baseScore + proximityScore;
    }
}

// Factory Pattern: Matcher factory
class MatcherFactory {
    public static Matcher createMatcher(MatcherType type) {
        switch (type) {
            case BASIC:
                return new BasicMatcher();
            case INTERESTS_BASED:
                return new InterestsBasedMatcher();
            case LOCATION_BASED:
                return new LocationBasedMatcher();
            default:
                return new BasicMatcher();
        }
    }
}

// -------------------- Dating App -------------------- //

// Facade Pattern: Dating app system
class DatingApp {
    private List<User> users;
    private List<ChatRoom> chatRooms;
    private Matcher matcher;
    
    // Singleton Pattern
    private static DatingApp instance;

    private DatingApp() {
        users = new ArrayList<>();
        chatRooms = new ArrayList<>();

         // Default to location-based matcher
        matcher = MatcherFactory.createMatcher(MatcherType.LOCATION_BASED);
    }

    public static DatingApp getInstance() {
        if (instance == null) {
            instance = new DatingApp();
        }
        return instance;
    }

    public void setMatcher(MatcherType type) {
        matcher = MatcherFactory.createMatcher(type);
    }

    public User createUser(String userId) {
        User user = new User(userId);
        users.add(user);
        return user;
    }

    public User getUserById(String userId) {
        for (User user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public List<User> findNearbyUsers(String userId, double maxDistance) {
        User user = getUserById(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        // Find users within maxDistance km
        List<User> nearbyUsers = LocationService.getInstance().findNearbyUsers(
                user.getProfile().getLocation(), maxDistance, users);

        // Filter out the user themselves        
        nearbyUsers.remove(user);

        // Filter out users that don't match preferences or have already been swiped       
        List<User> filteredUsers = new ArrayList<>();
        for (User otherUser : nearbyUsers) {
            // Skip users that have already been interacted with
            if (!user.hasInteractedWith(otherUser.getId())) {
                
                // Calculate match score
                double score = matcher.calculateMatchScore(user, otherUser);
                
                // If score is above 0, they meet basic preference criteria
                if (score > 0) {
                    filteredUsers.add(otherUser);
                }
            }
        }

        return filteredUsers;
    }

    public boolean swipe(String userId, String targetUserId, SwipeAction action) {
        User user = getUserById(userId);
        User targetUser = getUserById(targetUserId);

        if (user == null || targetUser == null) {
            System.out.println("User not found.");
            return false;
        }

        user.swipe(targetUserId, action);

        // Check if it's a match
        if (action == SwipeAction.RIGHT && targetUser.hasLiked(userId)) {
            // It's a match!
            String chatRoomId = userId + "_" + targetUserId;
            ChatRoom chatRoom = new ChatRoom(chatRoomId, userId, targetUserId);
            chatRooms.add(chatRoom);

            // Notify both users
            NotificationService.getInstance().notifyUser(userId, "You have a new match with " + targetUser.getProfile().getName() + "!");
            NotificationService.getInstance().notifyUser(targetUserId, "You have a new match with " + user.getProfile().getName() + "!");
            return true;
        }
        return false;
    }

    public ChatRoom getChatRoom(String user1Id, String user2Id) {
        for (ChatRoom chatRoom : chatRooms) {
            if (chatRoom.hasParticipant(user1Id) && chatRoom.hasParticipant(user2Id)) {
                return chatRoom;
            }
        }
        return null;
    }

    public void sendMessage(String senderId, String receiverId, String content) {
        ChatRoom chatRoom = getChatRoom(senderId, receiverId);
        if (chatRoom == null) {
            System.out.println("No chat room found between these users.");
            return;
        }

         // Notify the receiver
        chatRoom.addMessage(senderId, content);
        NotificationService.getInstance().notifyUser(receiverId, "New message from " + getUserById(senderId).getProfile().getName());
    }

    public void displayUser(String userId) {
        User user = getUserById(userId);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        user.displayProfile();
    }

    public void displayChatRoom(String user1Id, String user2Id) {
        ChatRoom chatRoom = getChatRoom(user1Id, user2Id);
        if (chatRoom == null) {
            System.out.println("No chat room found between these users.");
            return;
        }
        chatRoom.displayChat();
    }
}

// -------------------- Main -------------------- //

public class TinderClone {
    public static void main(String[] args) {
        // Get the dating app instance
        DatingApp app = DatingApp.getInstance();

         // Create users
        User user1 = app.createUser("user1");
        User user2 = app.createUser("user2");

        // Set user1 profile
        UserProfile profile1 = user1.getProfile();
        profile1.setName("Rohan");
        profile1.setAge(28);
        profile1.setGender(Gender.MALE);
        profile1.setBio("I am a software developer");
        profile1.addPhoto("rohan_photo1.jpg");
        profile1.addInterest("Coding", "Programming");
        profile1.addInterest("Travel", "Lifestyle");
        profile1.addInterest("Music", "Entertainment");

        // Setup user1 preferences
        Preference pref1 = user1.getPreference();
        pref1.addGenderPreference(Gender.FEMALE);
        pref1.setAgeRange(25, 30);
        pref1.setMaxDistance(10.0);
        pref1.addInterest("Coding");
        pref1.addInterest("Travel");

        // Setup user2 profile
        UserProfile profile2 = user2.getProfile();
        profile2.setName("Neha");
        profile2.setAge(27);
        profile2.setGender(Gender.FEMALE);
        profile2.setBio("Art teacher who loves painting and traveling.");
        profile2.addPhoto("neha_photo1.jpg");
        profile2.addInterest("Painting", "Art");
        profile2.addInterest("Travel", "Lifestyle");
        profile2.addInterest("Music", "Entertainment");

         // Setup user2 preferences
        Preference pref2 = user2.getPreference();
        pref2.addGenderPreference(Gender.MALE);
        pref2.setAgeRange(27, 30);
        pref2.setMaxDistance(15.0);
        pref2.addInterest("Coding");
        pref2.addInterest("Movies");

        // Set location for user1
        Location location1 = new Location();
        location1.setLatitude(1.01);
        location1.setLongitude(1.02);
        profile1.setLocation(location1);

         // Set location for user2 (Close to user1, within 5km)
        Location location2 = new Location();
        location2.setLatitude(1.03);
        location2.setLongitude(1.04);
        profile2.setLocation(location2);

        // Display user profiles
        System.out.println("---- User Profiles ----");
        app.displayUser("user1");
        app.displayUser("user2");

        // Find nearby users for user1 (within 5km)
        System.out.println("\n---- Nearby Users for user1 (within 5km) ----");
        List<User> nearbyUsers = app.findNearbyUsers("user1", 5.0);
        System.out.println("Found " + nearbyUsers.size() + " nearby users");
        for (User user : nearbyUsers) {
            System.out.println("- " + user.getProfile().getName() + " (" + user.getId() + ")");
        }

        // User1 swipes right on User2
        System.out.println("\n---- Swipe Actions ----");
        System.out.println("User1 swipes right on User2");
        app.swipe("user1", "user2", SwipeAction.RIGHT);

         // User2 swipes right on User1 (creating a match)
        System.out.println("User2 swipes right on User1");
        app.swipe("user2", "user1", SwipeAction.RIGHT);

        // Send messages in the chat room
        System.out.println("\n---- Chat Room ----");
        app.sendMessage("user1", "user2", "Hi Neha, Kaise ho?");
        
        app.sendMessage("user2", "user1", "Hi Rohan, Ma bdiya tum btao");
        
        // Display the chat room
        app.displayChatRoom("user1", "user2");
    }
}
