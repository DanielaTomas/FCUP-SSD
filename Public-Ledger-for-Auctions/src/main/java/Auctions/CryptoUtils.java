package Auctions;

import java.security.*;

public class CryptoUtils { //TODO update Transactions sign and verify signature methods

    public static byte[] sign(PrivateKey privateKey, byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        return signer.sign();
    }

    public static boolean verifySignature(PublicKey publicKey, byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(data);
        return verifier.verify(signature);
    }

}
