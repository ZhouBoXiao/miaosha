package miaosha.utils;

import lombok.extern.slf4j.Slf4j;
import miaosha.common.Constants;
import miaosha.common.SnowflakeIdWorker;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;


@Slf4j
public class Utils {


    private static final String HMAC_SHA512 = "HmacSHA512";
    public static Random random = new Random();

    /**
     * 获取当前的机器名
     *
     * @return 机器名
     */
    @NotNull
    public static String getHostName() {
        try {
            String canonicalName = InetAddress.getLocalHost().getCanonicalHostName();
            return canonicalName.replace(".", "_");
        } catch (UnknownHostException e) {
            log.error("## unknown host ##", e);
        }
        return "default_openapi";
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isBlank(ip) && !Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isBlank(ip) && !Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP。
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        } else {
            return request.getRemoteAddr();
        }
    }

    public static Object getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getRequestId() {
        String requestId = MDC.get(Constants.REQUESTID);
        if (requestId == null) {
            requestId = Long.toHexString(SnowflakeIdWorker.instance.nextId());
        }
        return requestId;
    }

    public static String hmac(final String key, final String message) {
        Mac sha512Hmac;
        String result = null;
        try {
            final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            sha512Hmac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(macData);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            log.info("## hmac compute failed ##", e);
        }
        return result;
    }

}
