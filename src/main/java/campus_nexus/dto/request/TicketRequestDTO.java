package campus_nexus.dto.request;

import campus_nexus.enums.PriorityLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TicketRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    private PriorityLevel priority = PriorityLevel.MEDIUM;

    @Pattern(regexp = "^$|\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String contactPhone;

    @Email(message = "Invalid email format")
    private String contactEmail;

    @Size(max = 3, message = "Maximum 3 images allowed")
    private List<@NotBlank @Size(max = 12_000_000, message = "Each image is too large") String> imageUrls = new ArrayList<>();
}