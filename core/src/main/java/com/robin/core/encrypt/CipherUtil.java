package com.robin.core.encrypt;

import com.google.common.collect.Lists;
import com.robin.core.base.util.CharUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;


@Slf4j
public class CipherUtil {
    private static final String DEFAULTALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";

    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
    public static final char[] avaiablechar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '/'};
    public static final byte[] m_datapadding = {0x7F};
    public static final byte[] m_ending = {0x00};
    //EXE header,pretent as a exe file
    public static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static final String DEFAULTKEY = "Robin1234567@123";
    public static final byte[] FILEENDS = {0x7F, 0x7F};
    private final static int KEY_SIZE = 512;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static byte[] initSecretKey() throws Exception {

        KeyGenerator kg = KeyGenerator.getInstance(DEFAULTALGORITHM);

        kg.init(56);

        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    private static SecretKey toKey(byte[] keybyte) throws Exception {
        SecretKeySpec key = new SecretKeySpec(keybyte, DEFAULTALGORITHM);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(DEFAULTALGORITHM);
        return skf.generateSecret(key);
    }

    public static SecretKey getKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(DEFAULTALGORITHM);
            kg.init(56);
            return kg.generateKey();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptByteWithKey(SecretKey key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptByte(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, toKey(key));
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return null;
    }


    public static byte[] decryptByte(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void decryptByte(byte[] key, InputStream is, OutputStream os) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULTALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            //CipherInputStream cis=new CipherInputStream(is, cipher);
            CipherOutputStream out = new CipherOutputStream(os, cipher);
            doCopy(is, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static byte[] encryptRSA(Key key, String content) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content.getBytes());
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return null;
    }

    public static byte[] encryptRSA(Key key, byte[] bytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return null;
    }

    public static byte[] signRSA(PrivateKey key, byte[] bytes) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(key);
            signature.update(bytes);
            return signature.sign();
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return null;
    }

    public static boolean verifyRSA(PublicKey key, byte[] content, byte[] sign) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(key);
            signature.update(content);
            return signature.verify(sign);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return false;
    }


    protected static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[64];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }

    public byte[] getDigest(InputStream stream) {
        try {
            int size = stream.available();
            byte[] bytes = new byte[size];
            stream.read(bytes);
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] retbyte = sha1.digest(bytes);
            return retbyte;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] getKeyByClassPath(String keyFile) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(CipherUtil.class.getClassLoader().getResourceAsStream(keyFile)))) {
            String readLineStr = null;
            while ((readLineStr = reader.readLine()) != null) {
                if (!readLineStr.startsWith("-")) {
                    builder.append(readLineStr);
                }
            }
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        return Base64.getDecoder().decode(builder.toString());
    }

    public static byte[] getKeyByPath(String keyFile) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(keyFile)))) {
            String readLineStr = null;
            while ((readLineStr = reader.readLine()) != null) {
                if (!readLineStr.startsWith("-")) {
                    builder.append(readLineStr);
                }
            }
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        return Base64.getDecoder().decode(builder.toString());
    }

    public static byte[] getPublicKeyByPath(String keyFile) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(keyFile)))) {
            String readLineStr = null;
            while ((readLineStr = reader.readLine()) != null) {
                if (!readLineStr.startsWith("-")) {
                    builder.append(readLineStr);
                }
            }
            String[] arr = builder.toString().split(" ");
            return Base64.getDecoder().decode(arr[1].toString());
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static byte[] getPublicKey(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String readLineStr = null;
            while ((readLineStr = reader.readLine()) != null) {
                if (!readLineStr.startsWith("-")) {
                    builder.append(readLineStr);
                }
            }
            String[] arr = builder.toString().split(" ");
            return Base64.getDecoder().decode(arr[1].toString());
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static PublicKey generatePublicKey(String algorithm, KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(algorithm).generatePublic(keySpec);
    }

    public static PrivateKey generatePrivateKey(String algorithm, KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
    }


    public static PublicKey readPublicKey(byte[] bytes) throws IOException {

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            byte[] sshRsa = new byte[in.readInt()];
            in.readFully(sshRsa);
            checkArgument(new String(sshRsa).equals("ssh-rsa"), "no RFC-4716 ssh-rsa");
            byte[] exp = new byte[in.readInt()];
            in.readFully(exp);
            byte[] mod = new byte[in.readInt()];
            in.readFully(mod);

            BigInteger e = new BigInteger(exp);
            BigInteger n = new BigInteger(mod);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        } catch (InvalidKeySpecException ex1) {
            throw new IllegalStateException(ex1);
        }
    }

    public static PrivateKey readPrivateKey(byte[] bytes) throws GeneralSecurityException, IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            checkArgument(in.read() == 48, "no id_rsa SEQUENCE");
            checkArgument(in.read() == 130, "no Version marker");
            in.skipBytes(5);
            BigInteger n = readAsnInteger(in);
            readAsnInteger(in);
            BigInteger e = readAsnInteger(in);
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(n, e);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static BigInteger readAsnInteger(DataInputStream in) throws IOException {
        checkArgument(in.read() == 2, "no INTEGER marker");
        int length = in.read();
        if (length >= 0x80) {
            byte[] extended = new byte[4];
            int bytesToRead = length & 0x7f;
            in.readFully(extended, 4 - bytesToRead, bytesToRead);
            length = new BigInteger(extended).intValue();
        }
        byte[] data = new byte[length];
        in.readFully(data);
        return new BigInteger(data);
    }

    public static List<String> generateRandomKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return Lists.newArrayList(privateKeyString, publicKeyString);
    }


    public static void main(String[] args) {
        try {
            byte[] bytes = CipherUtil.initSecretKey();
            String ret = Base64.getEncoder().encodeToString(bytes);
            System.out.println(ret);
            System.out.println(CharUtils.getInstance().retKeyword(118));
			/*String userPath = System.getProperty("user.home");
			//PrivateKey key=CipherUtil.readPrivateKey(CipherUtil.getKeyByPath(userPath+File.separator+".ssh"+File.separator+"id_rsa"));
			PrivateKey key=CipherUtil.readPrivateKey(CipherUtil.getKeyByPath("e:/dev/ssh/id_rsa"));
			//PublicKey key1=CipherUtil.readPublicKey(CipherUtil.getPublicKeyByPath(userPath+File.separator+".ssh"+File.separator+"id_rsa.pub"));
			PublicKey key1=CipherUtil.readPublicKey(CipherUtil.getPublicKeyByPath("e:/dev/ssh/id_rsa.pub"));
			JWTSigner signer = JWTSignerUtil.rs256(key);
			Map<String, Object> header = new HashMap<>();
			header.put("type", "JWT");
			header.put("alg", "RS256");
			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("key","111111");
			String jwt = JWTUtil.createToken(header, bodyMap, signer);
			System.out.println(jwt);
			JWTSigner signer1 = JWTSignerUtil.rs256(key1);
			JWTValidator.of(jwt).validateAlgorithm(signer1);
			JWT token = JWTUtil.parseToken(jwt);
			System.out.println(token);*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}  
