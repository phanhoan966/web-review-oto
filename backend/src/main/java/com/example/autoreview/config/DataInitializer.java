package com.example.autoreview.config;

import com.example.autoreview.entity.Review;
import com.example.autoreview.entity.User;
import com.example.autoreview.entity.VehicleBrand;
import com.example.autoreview.repository.ReviewRepository;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.repository.VehicleBrandRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository, VehicleBrandRepository brandRepository, ReviewRepository reviewRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            User enak = new User();
            enak.setUsername("Enak");
            enak.setEmail("enak@example.com");
            enak.setPasswordHash(passwordEncoder.encode("password123"));
            enak.getRoles().add("ROLE_USER");
            enak.setFollowers(85);
            enak.setRating(4.8);
            enak.setReviewCount(48);
            enak.setAvatarUrl("https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=200&q=60");
            enak.setCreatedAt(Instant.now());
            enak.setUpdatedAt(Instant.now());

            User alone = new User();
            alone.setUsername("Alone100");
            alone.setEmail("alone@example.com");
            alone.setPasswordHash(passwordEncoder.encode("password123"));
            alone.getRoles().add("ROLE_USER");
            alone.setFollowers(50);
            alone.setRating(4.7);
            alone.setReviewCount(50);
            alone.setAvatarUrl("https://images.unsplash.com/photo-1502764613149-7f1d229e2305?auto=format&fit=crop&w=200&q=60");
            alone.setCreatedAt(Instant.now());
            alone.setUpdatedAt(Instant.now());

            User toeto = new User();
            toeto.setUsername("toetkoihil999");
            toeto.setEmail("toet@example.com");
            toeto.setPasswordHash(passwordEncoder.encode("password123"));
            toeto.getRoles().add("ROLE_USER");
            toeto.setFollowers(32);
            toeto.setRating(4.6);
            toeto.setReviewCount(32);
            toeto.setAvatarUrl("https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=200&q=60");
            toeto.setCreatedAt(Instant.now());
            toeto.setUpdatedAt(Instant.now());

            userRepository.saveAll(List.of(enak, alone, toeto));

            VehicleBrand ford = new VehicleBrand();
            ford.setName("Ford");
            ford.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/3/3e/Ford_logo_flat.svg");
            ford.setFeatured(true);
            ford.setSortOrder(1);

            VehicleBrand honda = new VehicleBrand();
            honda.setName("Honda");
            honda.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/7/7b/Honda_Logo.svg");
            honda.setFeatured(true);
            honda.setSortOrder(2);

            VehicleBrand toyota = new VehicleBrand();
            toyota.setName("Toyota");
            toyota.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/9/9d/Toyota_carlogo.svg");
            toyota.setFeatured(true);
            toyota.setSortOrder(3);

            brandRepository.saveAll(List.of(ford, honda, toyota));

            Review civic = new Review();
            civic.setTitle("Honda Civic 2022 Review");
            civic.setExcerpt("Thiết kế thể thao, tiết kiệm nhiên liệu và an toàn vượt trội.");
            civic.setContent("Đánh giá chi tiết Honda Civic 2022.");
            civic.setHeroImageUrl("https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?auto=format&fit=crop&w=1400&q=80");
            civic.setVehicleModel("Civic");
            civic.setVehicleYear(2022);
            civic.setFuelType("Xăng");
            civic.setPriceSegment("50 triệu/mua");
            civic.setLikes(24);
            civic.setCommentsCount(12);
            civic.setViews(900);
            civic.setPublishedAt(Instant.now().minus(3, ChronoUnit.HOURS));
            civic.setAuthor(enak);
            civic.setBrand(honda);

            Review camry = new Review();
            camry.setTitle("2024 Toyota Camry Review");
            camry.setExcerpt("Nội thất sang trọng, trải nghiệm lái mượt mà.");
            camry.setContent("Toyota Camry 2024 nổi bật về độ tin cậy.");
            camry.setHeroImageUrl("https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?auto=format&fit=crop&w=1400&q=80");
            camry.setVehicleModel("Camry");
            camry.setVehicleYear(2024);
            camry.setFuelType("Hybrid");
            camry.setPriceSegment("50 triệu/mua");
            camry.setLikes(32);
            camry.setCommentsCount(15);
            camry.setViews(850);
            camry.setPublishedAt(Instant.now().minus(5, ChronoUnit.HOURS));
            camry.setAuthor(alone);
            camry.setBrand(toyota);

            Review ranger = new Review();
            ranger.setTitle("Ford Ranger 2023 Review");
            ranger.setExcerpt("Hiệu suất mạnh mẽ và bền bỉ.");
            ranger.setContent("Ford Ranger 2023 với khả năng địa hình vượt trội.");
            ranger.setHeroImageUrl("https://images.unsplash.com/photo-1502877828070-33b167ad6860?auto=format&fit=crop&w=1400&q=80");
            ranger.setVehicleModel("Ranger");
            ranger.setVehicleYear(2023);
            ranger.setFuelType("Dầu");
            ranger.setPriceSegment("50 triệu/mua");
            ranger.setLikes(28);
            ranger.setCommentsCount(18);
            ranger.setViews(760);
            ranger.setPublishedAt(Instant.now().minus(1, ChronoUnit.DAYS));
            ranger.setAuthor(toeto);
            ranger.setBrand(ford);

            reviewRepository.saveAll(List.of(civic, camry, ranger));
        };
    }
}
