package com.finalproject.hotelbooking.config;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.model.Role;
import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.repository.RoomRepository;
import com.finalproject.hotelbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public DataSeeder(
            UserRepository userRepository,
            RoomRepository roomRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        seedUsers();
        seedRooms();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@hotel.com")) {
            AppUser admin = new AppUser();
            admin.setName("Admin User");
            admin.setEmail("admin@hotel.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
        if (!userRepository.existsByEmail("user@hotel.com")) {
            AppUser user = new AppUser();
            user.setName("Demo User");
            user.setEmail("user@hotel.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            userRepository.save(user);
        }
    }

    private void seedRooms() {
        if (roomRepository.count() > 0) {
            return;
        }
        roomRepository.save(room(
                "Skyline Deluxe Room",
                "Seoul",
                "Deluxe",
                "A bright city-view room with a queen bed, work desk, private bathroom, and fast Wi-Fi.",
                new BigDecimal("95.00"),
                2,
                "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1000&q=80"
        ));
        roomRepository.save(room(
                "Family Comfort Suite",
                "Busan",
                "Family",
                "A larger room for families with two beds, a seating area, ocean-inspired decor, and breakfast access.",
                new BigDecimal("135.00"),
                4,
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=80"
        ));
        roomRepository.save(room(
                "Budget Standard Room",
                "Incheon",
                "Standard",
                "A clean and simple room for short stays with essential amenities and convenient airport access.",
                new BigDecimal("70.00"),
                2,
                "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=1000&q=80"
        ));
        roomRepository.save(room(
                "Premium Business Room",
                "Daegu",
                "Business",
                "A quiet business room with a large desk, lounge chair, high-speed internet, and late check-in support.",
                new BigDecimal("110.00"),
                2,
                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1000&q=80"
        ));
    }

    private Room room(String name, String city, String type, String description, BigDecimal price, int capacity, String imagePath) {
        Room room = new Room();
        room.setName(name);
        room.setCity(city);
        room.setRoomType(type);
        room.setDescription(description);
        room.setPricePerNight(price);
        room.setCapacity(capacity);
        room.setImagePath(imagePath);
        room.setAvailable(true);
        return room;
    }
}
