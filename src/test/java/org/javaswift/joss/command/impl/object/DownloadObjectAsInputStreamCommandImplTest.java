package org.javaswift.joss.command.impl.object;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.javaswift.joss.command.impl.core.BaseCommandTest;
import org.javaswift.joss.exception.CommandException;
import org.javaswift.joss.instructions.DownloadInstructions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.javaswift.joss.command.impl.object.DownloadObjectAsByteArrayCommandImpl.CONTENT_LENGTH;
import static org.javaswift.joss.command.impl.object.DownloadObjectAsByteArrayCommandImpl.ETAG;

public class DownloadObjectAsInputStreamCommandImplTest extends BaseCommandTest {

    @Before
    public void setup() throws IOException {
        super.setup();
    }

    protected void prepareBytes(byte[] bytes, String md5) {
        final List<Header> headers = new ArrayList<Header>();
        prepareHeader(response, ETAG, md5 == null ? DigestUtils.md5Hex(bytes) : md5, headers);
        prepareHeader(response, CONTENT_LENGTH, "3", headers);
        prepareHeadersForRetrieval(response, headers);
        setHttpEntity(new ByteArrayEntity(bytes));
    }

    @Test
    public void downloadSuccess() throws IOException {
        byte[] bytes = new byte[] { 0x01, 0x02, 0x03};
        prepareBytes(bytes, null);
        InputStream result = new DownloadObjectAsInputStreamCommandImpl(this.account, httpClient, defaultAccess, getObject("objectname"), new DownloadInstructions()).call();
        byte[] downloaded = IOUtils.toByteArray(result);
        result.close();
        assertEquals(bytes.length, downloaded.length);
    }

    @Test
    public void md5Mismatch() throws IOException {
        prepareBytes(new byte[] { 0x01}, "cafebabe"); // non-matching MD5
        try {
            new DownloadObjectAsInputStreamCommandImpl(this.account, httpClient, defaultAccess, getObject("objectname"), new DownloadInstructions());
        } catch (CommandException err) {
            fail("Downloading as an inputstream does not check for the MD5 checksum, so therefore should not throw an error");
        }
    }

    @Test
    public void isSecure() throws IOException {
        prepareBytes(new byte[] { 0x01, 0x02, 0x03}, null);
        isSecure(new DownloadObjectAsInputStreamCommandImpl(this.account, httpClient, defaultAccess,
                getObject("objectname"), new DownloadInstructions()));
    }
}
