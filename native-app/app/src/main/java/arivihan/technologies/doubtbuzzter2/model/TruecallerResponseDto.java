package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TruecallerResponseDto implements Serializable {
    private String email;
    private String phone_number;
    private boolean phone_number_verified;
    private String phone_number_country_code;
    private String given_name;
    private String family_name;
    private String picture;
    private String token;
}
