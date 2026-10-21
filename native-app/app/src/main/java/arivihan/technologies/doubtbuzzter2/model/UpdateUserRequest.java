package arivihan.technologies.doubtbuzzter2.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String username;
    private String imageUrl;
    private String address;
    private Long phoneNumber;
}
