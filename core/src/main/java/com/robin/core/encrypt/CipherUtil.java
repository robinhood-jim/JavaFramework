package com.robin.core.encrypt;

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.robin.core.base.util.CharUtils;
import com.robin.core.base.util.IOUtils;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.common.ssh.components.jce.JCEProvider;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;


@Slf4j
public class CipherUtil {
    private static final String DEFAULTALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";
    private static final String[] CONFUSEDSTRS = {"i", "I", "l", "O", "0", "1"};

    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
    public static final char[] avaiablechar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '/'};
    public static final byte[] m_datapadding = {0x7F};
    public static final byte[] m_ending = {0x00};
    //EXE header,pretent as a exe file
    public static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static final String DEFAULTKEY = "Robin1234567@123";
    public static final byte[] FILEENDS = {0x7F, 0x7F};
    private static final int KEY_SIZE = 512;
    public static final String PUBLICKEYPERFIX="PUBLIC KEY";
    public static final String PRIVATEKEYPERFIX="PRIVATE KEY";

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static byte[] initSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(DEFAULTALGORITHM);
        kg.init(56);
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    private static SecretKey toKey(byte[] keybyte) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
            log.error("{}", ex.getMessage());
        }
        return null;
    }

    public static byte[] encryptByteWithKey(SecretKey key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
        return null;
    }

    public static byte[] encryptByte(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, toKey(key));
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
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
            log.error("{}", ex.getMessage());
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
    public static byte[] getMd5(byte[] bytes) {
        byte[] b = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            b = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static byte[] getKeyByClassPath(String keyFile) {
        return getKeyByInputStream(CipherUtil.class.getClassLoader().getResourceAsStream(keyFile));
    }

    public static byte[] getKeyByInputStream(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
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


    public static byte[] getKeyBytesByPath(String keyFile) throws IOException {
        return getKeyBytes(new FileInputStream(keyFile));
    }

    public static byte[] getKeyBytes(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String readLineStr = null;
            while ((readLineStr = reader.readLine()) != null) {
                if (!readLineStr.startsWith("-")) {
                    builder.append(readLineStr);
                }
            }
            String[] arr = builder.toString().split(" ");
            if(arr.length==2) {
                return Base64.getDecoder().decode(arr[1].toString());
            }else{
                return Base64.getDecoder().decode(arr[0].toString());
            }
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static PublicKey generatePublicKey(String algorithm, KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(algorithm).generatePublic(keySpec);
    }


    @Deprecated
    public static PublicKey readPublicKey(byte[] bytes) throws IOException {

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            byte[] sshRsa = new byte[in.readInt()];
            in.readFully(sshRsa);
            checkArgument("ssh-rsa".equals(new String(sshRsa)), "no RFC-4716 ssh-rsa");
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
    public static PublicKey readPublicKeyByPem(InputStream inputStream) throws Exception{
        PublicKey publicKey ;
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        try {
            IOUtils.copyBytes(inputStream,byteArrayOutputStream);
            JCEProvider.enableBouncyCastle(false);
            SshPublicKey pair = SshKeyUtils.getPublicKey(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            publicKey=pair.getJCEPublicKey();
        } catch (Exception ex) {
            byte[] bytes = CipherUtil.getKeyByInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            publicKey= generatePublicKey("RSA",new X509EncodedKeySpec(bytes));
        }finally {
            IOUtils.closeStream(byteArrayOutputStream);
        }
        return publicKey;
    }
    public static PrivateKey readPrivateKeyByPem(InputStream inputStream) throws Exception {
        PemReader pemReader = null;
        PrivateKey privKey = null;
        String tmpPath = System.getProperty(CharUtils.getInstance().retKeyword(115)) + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator ;
        try (ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream()){
            IOUtils.copyBytes(inputStream,byteArrayOutputStream);
            pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
            PemObject pemObject = pemReader.readPemObject();
            if (pemObject.getType().endsWith("RSA PRIVATE KEY")) {
                // 取得私钥  for PKCS#1
                org.bouncycastle.asn1.pkcs.RSAPrivateKey asn1PrivKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(pemObject.getContent());
                RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privKey = keyFactory.generatePrivate(rsaPrivKeySpec);
            }else if(pemObject.getType().endsWith("OPENSSH PRIVATE KEY")){
                if(!FileUtil.exist(tmpPath+"openssh.pem")) {
                    JCEProvider.enableBouncyCastle(false);
                    SshKeyPair pair = SshKeyUtils.getPrivateKey(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "");
                    SshKeyUtils.savePrivateKey(pair, "", "", new File(tmpPath + "openssh.pem"));
                }
                pemReader=new PemReader(new FileReader(tmpPath+"openssh.pem"));
                org.bouncycastle.asn1.pkcs.RSAPrivateKey asn1PrivKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(pemReader.readPemObject().getContent());
                RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                privKey = keyFactory.generatePrivate(rsaPrivKeySpec);
            }
            else if (pemObject.getType().endsWith("PRIVATE KEY")) {
                //取得私钥 for PKCS#8
                PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                privKey = kf.generatePrivate(privKeySpec);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (pemReader != null) {
                    pemReader.close();
                }
            } catch (Exception ex) {

            }
        }
        return privKey;
    }
    @Deprecated
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
    public static PrivateKey generatePrivateKey(String algorithm, KeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
    }


    public static List<String> generateRandomKey() throws NoSuchAlgorithmException {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

            // 初始化密钥对生成器
            keyPairGen.initialize(2048, new SecureRandom());
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
        }catch (NoSuchAlgorithmException ex){
            throw ex;
        }finally {

        }
    }
    public static String bytesToHexString1(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static byte[] LongToBytes(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }
    public static byte[] hexStringToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    public static byte[] getEncryptKey(byte[] bytes){
        if(bytes.length==16 || bytes.length==32){
            return bytes;
        }else{
            return getMd5(bytes);
        }
    }
    public static String generateRandomKey(int range, int num, Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int randint = random.nextInt(range);
            builder.append(CipherUtil.avaiablechar[randint]);
        }
        return builder.toString();
    }
    public static List<String> getConfusedName(int length, Random random) {
        StringBuilder builder = new StringBuilder();
        StringBuilder builder1 = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int pos = random.nextInt(CONFUSEDSTRS.length);
            builder1.append(pos);
            builder.append(CONFUSEDSTRS[pos]);
        }
        List<String> retList = new ArrayList<>();
        retList.add(builder.toString());
        retList.add(builder1.toString());
        return retList;
    }
    public static String decodeConfusedNameByCode(String code){
        StringBuilder builder = new StringBuilder();
        for(char input:code.toCharArray()){
            builder.append(CONFUSEDSTRS[Integer.parseInt(String.valueOf(input))]);
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        try {
            byte[] bytes=hexStringToBytes("9AF16B867075453592358C8BDB17CBF1");
            System.out.println(bytes);
            System.out.println(bytesToHexString(bytes));

            /*byte[] bytes = CipherUtil.initSecretKey();
            String ret = Base64.getEncoder().encodeToString(bytes);
            System.out.println(ret);
            System.out.println(CharUtils.getInstance().retKeyword(118));
            List<String> pair=CipherUtil.generateRandomKey();
            StringBuilder builder1=new StringBuilder();
            builder1.append("-----BEGIN PRIVATE KEY-----\n");
            builder1.append(pair.get(0)).append("\n");
            builder1.append("-----END PRIVATE KEY-----");
            InputStream stream=new ByteArrayInputStream(builder1.toString().getBytes());
            PrivateKey key1= readPrivateKeyByPem(stream);
            builder1.delete(0,builder1.length());
            builder1.append("-----BEGIN PUBLIC KEY-----\n");
            builder1.append(pair.get(1)).append("\n");
            builder1.append("-----END PUBLIC KEY-----");
            InputStream stream1=new ByteArrayInputStream(builder1.toString().getBytes());
            PublicKey key2=readPublicKeyByPem(stream1);
            System.out.println(pair);

			String userPath = System.getProperty("user.home");
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
