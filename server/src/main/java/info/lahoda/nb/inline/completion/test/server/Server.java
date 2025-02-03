package info.lahoda.nb.inline.completion.test.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class Server implements LanguageServer, LanguageClientAware {

    public static void main(String[] args) throws Exception {
        doStartServer(System.in, System.out).get();
    }

    public static Future<Void> doStartServer(InputStream in, OutputStream out) {
        Server server = new Server();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out, false, new PrintWriter(System.err));

        server.connect(launcher.getRemoteProxy());

        return launcher.startListening();
    }

    private LanguageClient client;

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capa = new ExhancedServerCapabilities();
        capa.setTextDocumentSync(TextDocumentSyncKind.Full);
        ServerInfo info = new ServerInfo("test.server");
        InitializeResult initResult = new InitializeResult(capa, info);
        CompletableFuture<InitializeResult> result = new CompletableFuture<>();
        result.complete(initResult);
        return result;
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        CompletableFuture<Object> result = new CompletableFuture<>();
        result.complete(null);
        return null;
    }

    @Override
    public void exit() {
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return new TextDocumentServiceImpl();
    }

    public TextDocumentService getgetEnhancedTextDocumentService() {
        return getTextDocumentService();
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return new WorkspaceServiceImpl();
    }

    public void setTrace(SetTraceParams params) {
        //TODO
    }

    public static class ExhancedServerCapabilities extends ServerCapabilities {

        public ExhancedServerCapabilities() {
        }

        public boolean getInlineCompletionProvider() {
            return true;
        }
    }
}
