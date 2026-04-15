import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtEncodeAndDecodeTest {

    @Test
    public void testDecodeJwt() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJwZXJtaXNzaW9ucyI6WyJVU0VSIiwiVVNFUi5MSVNUIiwiVVNFUi5BREQiLCJVU0VSLkRFTEVURSIsIlVTRVIuRURJVCIsIlJPTEUiLCJST0xFLkxJU1QiLCJST0xFLkFERCIsIlJPTEUuREVMRVRFIiwiUk9MRS5FRElUIiwiUk9MRS5BU1NJR04iLCJQRVJNSVNTSU9OIiwiUEVSTUlTU0lPTi5MSVNUIiwiUEVSTUlTU0lPTi5BREQiLCJQRVJNSVNTSU9OLkRFTEVURSIsIlBFUk1JU1NJT04uRURJVCIsIlBFUk1JU1NJT04uQVNTSUdOIiwiQkxPRyIsIkJMT0cuTElTVCIsIkJMT0cuQUREIiwiQkxPRy5ERUxFVEUiLCJCTE9HLkVESVQiLCJCTE9HLlNUQVIiLCJCTE9HLkNPTU1FTlQiLCJDT01NRU5ULkxJU1QiLCJDT01NRU5ULkFERCIsIkNPTU1FTlQuREVMRVRFIiwiUFJPQkxFTSIsIlBST0JMRU0uTElTVCIsIlBST0JMRU0uQUREIiwiUFJPQkxFTS5ERUxFVEUiLCJQUk9CTEVNLkVESVQiLCJMQU5HVUFHRSIsIkxBTkdVQUdFLkxJU1QiLCJURVNUQ0FTRSIsIlRFU1RDQVNFLkxJU1QiLCJURVNUQ0FTRS5ERUxFVEUiLCJURVNUQ0FTRS5FRElUIiwiVEVTVENBU0UuQUREIl0sInVzZXJJZCI6MjA0NDMxMjk5MzI0MzYxOTMyOSwidXNlcm5hbWUiOiJhZG1pbnRlc3R1c2VyIiwiaWF0IjoxNzc2MjQwNjM0LCJleHAiOjE3NzYyNDc4MzR9.yXMmotEKaRQF5yWOYq03yH_5UFWs1q46yge26oRhLPXbj_ImTqxdMvnmiO_uYyQYQ47tZSeOCkHgTZAIZH_GUg"; // 请在此处替换为要解析的实际Token

        String[] parts = token.split("\\.");
        if (parts.length >= 2) {
            String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            System.out.println("Header解析结果: " + header);
            System.out.println("Payload解析结果: " + payload);
        } else {
            System.out.println("无效的JWT Token格式");
        }
    }
}
