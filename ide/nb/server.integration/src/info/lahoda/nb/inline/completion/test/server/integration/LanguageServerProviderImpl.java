package info.lahoda.nb.inline.completion.test.server.integration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

@MimeRegistration(mimeType="text/x-java", service=LanguageServerProvider.class)
public class LanguageServerProviderImpl implements LanguageServerProvider {

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        try {
            File server = InstalledFileLocator.getDefault().locate("test-server", null, false);
            String cp = Arrays.stream(server.listFiles()).map(f -> f.getAbsolutePath()).collect(Collectors.joining(System.getProperty("path.separator")));
            Process p = new ProcessBuilder("java",
                               "-classpath", cp,
                               "info.lahoda.nb.inline.completion.test.server.Server").redirectError(ProcessBuilder.Redirect.INHERIT).start();
            return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
}
