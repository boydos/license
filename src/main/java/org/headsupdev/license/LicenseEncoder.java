package org.headsupdev.license;

import org.bouncycastle.util.encoders.Base64;
import org.headsupdev.license.exception.InvalidFormatException;

import javax.crypto.Cipher;
import java.security.*;
import java.io.*;

/**
 * TODO add a description
 *
 * @author Andrew Williams
 * @version $Id: LicenseEncoder.java 76 2012-03-17 23:17:03Z handyande $
 * @since 1.0
 */
public class LicenseEncoder
{
    private Key priv, shared;

    public LicenseEncoder()
    {

    }

    public void setPrivateKey( Key key )
    {
        this.priv = key;
    }

    public void setSharedKey( Key key )
    {
        this.shared = key;
    }

    public String encodeLicense( License in )
        throws NoSuchAlgorithmException, InvalidKeyException, InvalidFormatException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in.getProperties().store( out, in.getLicenseTitle() );
        }
        catch ( IOException e )
        {
            throw new InvalidFormatException( e );
        }

        return encode( out.toByteArray() );
    }

    protected String encode( byte[] data )
        throws NoSuchAlgorithmException, InvalidKeyException
    {
        if ( priv == null || shared == null )
        {
            throw new IllegalStateException( "Keys must be set before decoding the license" );
        }

        MessageDigest digester = MessageDigest.getInstance( "MD5" );
        digester.update( data );
        byte[] digest = digester.digest();

        try
        {
            Cipher cipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
            cipher.init( Cipher.ENCRYPT_MODE, priv );
            byte[] signature = cipher.doFinal( digest );

            byte[] decompressed = new byte[ data.length + signature.length + 2 ];
            System.arraycopy( data, 0, decompressed, 0, data.length );
            decompressed[ data.length ] = 0;
            decompressed[ data.length + 1 ] = 0;
            System.arraycopy( signature, 0, decompressed, data.length + 2, signature.length );

            Cipher cipher2 = Cipher.getInstance( "DES/ECB/PKCS5Padding" );
            cipher2.init( Cipher.ENCRYPT_MODE, shared );
            byte[] out = cipher2.doFinal( decompressed );

            return new String( Base64.encode( out ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return "";
        }
    }
}