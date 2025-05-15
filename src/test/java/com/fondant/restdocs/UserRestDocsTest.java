package com.fondant.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fondant.global.config.SecurityConfig;
import com.fondant.infra.jwt.application.JWTUtil;
import com.fondant.test.repository.UserTestRepository;
import com.fondant.global.annotation.WithMockCustomUser;
import com.fondant.user.application.UserService;
import com.fondant.user.application.dto.CustomUserDetails;
import com.fondant.user.domain.entity.*;
import com.fondant.user.presentation.dto.request.DeliveryAddressAddRequest;
import com.fondant.user.presentation.dto.request.DeliveryAddressUpdateRequest;
import com.fondant.user.presentation.dto.request.UserUpdateRequest;
import com.fondant.user.presentation.dto.response.DeliveryAddressResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "localhost", uriPort = 8080)
@Import(SecurityConfig.class)
@Transactional
public class UserRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserTestRepository userRepository;

    @MockitoSpyBean
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    private static final String BASE_URL = "/api/user";

    public static FieldDescriptor[] commonResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("요청 성공 여부 (true/false)"),
                fieldWithPath("message").description("응답 메시지"),
                fieldWithPath("response").description("응답 데이터")
        };
    }

    private DeliveryAddressAddRequest address1;
    private DeliveryAddressAddRequest address2;
    private String mockToken;
    private UserEntity testUser;

    @BeforeEach
    public void setUp() {

        address1 = DeliveryAddressAddRequest.builder()
                .deliveryAddress("서울시 강남구")
                .postCode("12345")
                .alias("alias")
                .receiverName("홍길동")
                .receiverPhoneNumber("010-1234-5678")
                .isPrimary(true)
                .build();

        address2 = DeliveryAddressAddRequest.builder()
                .deliveryAddress("서울시 서초구")
                .postCode("67890")
                .alias("alias2")
                .receiverName("김철수")
                .receiverPhoneNumber("010-9876-5432")
                .isPrimary(false)
                .build();

        testUser = userRepository.save(
                UserEntity.builder()
                        .snsType(SNSType.NAVER)
                        .name("test-user")
                        .phoneNumber("010-0000-0000")
                        .email("test-user@example.com")
                        .birth(Date.valueOf(LocalDate.of(2001, 1, 1)))
                        .nickname("test-user-nickname")
                        .profileUrl("https://example.com")
                        .createAt(Date.valueOf(LocalDate.of(2025, 1, 1)).toLocalDate())
                        .gender(Gender.FEMALE)
                        .role(UserRole.USER)
                        .build()
        );

        mockToken = jwtUtil.generateToken("access", testUser.getId(), "USER", 60 * 10 * 1000L);
    }

    @Test
    @WithMockCustomUser
    void getUserInfo() throws Exception {
        mockMvc.perform(get(BASE_URL + "/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andDo(document("user/get-user-info",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}")
                                        .optional(),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                                        .optional()
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("phoneNumber").description("전화번호").optional(),
                                fieldWithPath("verifiedPhone").description("전화번호 인증 여부").optional(),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("birth").description("생일").optional(),
                                fieldWithPath("nickname").description("닉네임").optional(),
                                fieldWithPath("profileUrl").description("프로필 사진 URL").optional(),
                                fieldWithPath("gender").description("성별").optional()
                        })
                ));
    }

    @Test
    @WithMockCustomUser
    void updateUserInfo() throws Exception {

        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("updated name")
                .phoneNumber("010-9999-9999")
                .verifiedPhone(true)
                .email("<EMAIL>")
                .nickname("updated nickname")
                .profileUrl("https://updated-profile.com/image.jpg")
                .gender(Gender.FEMALE)
                .birth(Date.valueOf("2000-01-01"))
                .build();

        mockMvc.perform(patch(BASE_URL + "/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("user/update-user-info",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("변경할 사용자 이름").optional(),
                                fieldWithPath("phoneNumber").description("변경할 전화번호").optional(),
                                fieldWithPath("verifiedPhone").description("전화번호 인증 여부").optional(),
                                fieldWithPath("email").description("변경할 이메일").optional(),
                                fieldWithPath("nickname").description("변경할 닉네임").optional(),
                                fieldWithPath("profileUrl").description("변경할 프로필 사진 URL").optional(),
                                fieldWithPath("gender").description("변경할 성별").optional(),
                                fieldWithPath("birth").description("변경할 생일").optional()
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("response").description("응답 데이터")
                        )
                ));

        UserEntity updatedUser = userRepository.findByEmail(request.email());

        assertThat(updatedUser.getName()).isEqualTo(request.name());
        assertThat(updatedUser.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(updatedUser.isVerifiedPhone()).isEqualTo(request.verifiedPhone());
        assertThat(updatedUser.getEmail()).isEqualTo(request.email());
        assertThat(updatedUser.getNickname()).isEqualTo(request.nickname());
        assertThat(updatedUser.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(updatedUser.getProfileUrl()).isEqualTo(request.profileUrl());
        assertThat(updatedUser.getGender()).isEqualTo(request.gender());
    }

    @Test
    @WithMockCustomUser
    void getUserDeliveryAddress() throws Exception {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        userService.addDeliveryAddress(userDetails.getUserId(), address1);
        userService.addDeliveryAddress(userDetails.getUserId(), address2);

        entityManager.flush();
        entityManager.clear();

        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), 100_000L);

        mockMvc.perform(get(BASE_URL + "/address/")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + access))
                .andExpect(status().isOk())
                .andDo(document("user/get-user-delivery-address",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}")
                                        .optional(),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                                        .optional()
                        ),
                        responseFields(
                                commonResponseFields()
                        ).andWithPrefix("response.", new FieldDescriptor[]{
                                fieldWithPath("[]").description("배송지 목록"),
                                fieldWithPath("[].id").description("배송지 ID"),
                                fieldWithPath("[].deliveryAddress").description("배송지 주소"),
                                fieldWithPath("[].postCode").description("우편번호"),
                                fieldWithPath("[].alias").description("배송지 별명"),
                                fieldWithPath("[].receiverName").description("수령인 이름"),
                                fieldWithPath("[].receiverPhoneNumber").description("수령인 연락처"),
                                fieldWithPath("[].isPrimary").description("기본 배송지 여부")
                        })
                ));
    }

    @Test
    @WithMockCustomUser
    void addUserDeliveryAddress() throws Exception {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), 100_000L);

        mockMvc.perform(post(BASE_URL + "/address/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address1))
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + access))
                .andExpect(status().isOk())
                .andDo(document("user/add-user-delivery-address",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}")
                                        .optional(),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                                        .optional()
                        ),
                        requestFields(
                                fieldWithPath("deliveryAddress").description("배송지 주소"),
                                fieldWithPath("postCode").description("우편번호"),
                                fieldWithPath("alias").description("배송지 별명"),
                                fieldWithPath("receiverName").description("수령인 이름"),
                                fieldWithPath("receiverPhoneNumber").description("수령인 연락처"),
                                fieldWithPath("isPrimary").description("기본 배송지 여부")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                ));

        assertThat(userService.getDeliveryAddress(userDetails.getUserId()).get(0))
                .satisfies(address -> {
                    assertThat(address.deliveryAddress()).isEqualTo(address1.deliveryAddress());
                    assertThat(address.postCode()).isEqualTo(address1.postCode());
                    assertThat(address.alias()).isEqualTo(address1.alias());
                    assertThat(address.receiverName()).isEqualTo(address1.receiverName());
                    assertThat(address.receiverPhoneNumber()).isEqualTo(address1.receiverPhoneNumber());
                    assertThat(address.isPrimary()).isEqualTo(address1.isPrimary());
                });
    }

    @Test
    @WithMockCustomUser
    void updateUserDeliveryAddress() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        userService.addDeliveryAddress(userDetails.getUserId(), address2);

        entityManager.flush();
        entityManager.clear();

        List<DeliveryAddressResponse> deliveryAddresses = userService.getDeliveryAddress(userDetails.getUserId());
        DeliveryAddressResponse savedAddress = deliveryAddresses.get(deliveryAddresses.size() - 1);

        DeliveryAddressUpdateRequest updateRequest = DeliveryAddressUpdateRequest.builder()
                .id(savedAddress.id())
                .deliveryAddress("대구광역시 동구")
                .postCode("61234")
                .alias("update")
                .receiverName("이훈이")
                .receiverPhoneNumber("010-9226-5222")
                .isPrimary(false)
                .build();

        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(),100_000L);

        mockMvc.perform(patch(BASE_URL + "/address/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + access))
                .andExpect(status().isOk())
                .andDo(document("user/update-user-delivery-address",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}")
                                        .optional(),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                                        .optional()
                        )
                        ,requestFields(
                                fieldWithPath("id").description("배송지 주소의 id"),
                                fieldWithPath("deliveryAddress").description("배송지 주소"),
                                fieldWithPath("postCode").description("우편번호"),
                                fieldWithPath("alias").description("배송지 별명"),
                                fieldWithPath("receiverName").description("수령인 이름"),
                                fieldWithPath("receiverPhoneNumber").description("수령인 연락처"),
                                fieldWithPath("isPrimary").description("기본 배송지 여부")
                        )
                        ,
                        responseFields(
                                commonResponseFields()
                        )
                ));

        assertThat(userService.getDeliveryAddress(userDetails.getUserId()).get(0))
                .satisfies(address -> {
                    assertThat(address.deliveryAddress()).isEqualTo(updateRequest.deliveryAddress());
                    assertThat(address.postCode()).isEqualTo(updateRequest.postCode());
                    assertThat(address.alias()).isEqualTo(updateRequest.alias());
                    assertThat(address.receiverName()).isEqualTo(updateRequest.receiverName());
                    assertThat(address.receiverPhoneNumber()).isEqualTo(updateRequest.receiverPhoneNumber());
                    assertThat(address.isPrimary()).isEqualTo(updateRequest.isPrimary());
                });
    }

    @Test
    @WithMockCustomUser
    void deleteUserDeliveryAddress() throws Exception {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        userService.addDeliveryAddress(userDetails.getUserId(), address1);

        entityManager.flush();
        entityManager.clear();

        List<DeliveryAddressResponse> deliveryAddresses = userService.getDeliveryAddress(userDetails.getUserId());
        Long deliveryAddressId = deliveryAddresses.get(deliveryAddresses.size() - 1).id();

        String access = jwtUtil.generateToken("access", userDetails.getUserId(), UserRole.USER.toString(), 100_000L);

        mockMvc.perform(delete(BASE_URL + "/address/{deliveryAddressId}",deliveryAddressId)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer " + access))
                .andExpect(status().isOk())
                .andDo(document("user/delete-user-delivery-address",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Bearer {access-token}")
                                        .optional(),
                                headerWithName(HttpHeaders.CONTENT_TYPE)
                                        .description("application/json")
                                        .optional()
                        ),
                        pathParameters(
                                parameterWithName("deliveryAddressId").description("배송지 주소의 id")
                        ),
                        responseFields(
                                commonResponseFields()
                        )
                ));
    }
}