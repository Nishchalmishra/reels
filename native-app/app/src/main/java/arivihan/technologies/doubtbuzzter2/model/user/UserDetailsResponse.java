package arivihan.technologies.doubtbuzzter2.model.user;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import arivihan.technologies.doubtbuzzter2.model.user.batch.BatchDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@ToString
@Nullable
public class UserDetailsResponse implements Serializable {

    private String userId;
    private String userName;
    private Long phoneNumber;
    private Boolean isNewUser;

    private List<BatchDetail> batches;

}