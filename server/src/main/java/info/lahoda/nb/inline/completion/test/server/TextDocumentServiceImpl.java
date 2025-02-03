package info.lahoda.nb.inline.completion.test.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.TextDocumentService;

public class TextDocumentServiceImpl implements TextDocumentService {

    private static final Gson GSON = new Gson();
    private final Map<String, String> url2Content = new HashMap<>();

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        url2Content.put(params.getTextDocument().getUri(), params.getTextDocument().getText());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        url2Content.put(params.getTextDocument().getUri(), params.getContentChanges().get(0).getText());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams dctdp) {
    }

    @Override
    public void didSave(DidSaveTextDocumentParams dstdp) {
    }

    @JsonRequest("textDocument/inlineCompletion")
    public CompletableFuture<InlineCompletionItem[]> inlineCompletion(JsonObject paramsObject) {
        InlineCompletionParams params = GSON.fromJson(paramsObject, InlineCompletionParams.class);
        String sourceURIText = params.textDocument.getUri();

        if (!sourceURIText.contains("server-test/work")) {
            return CompletableFuture.completedFuture(new InlineCompletionItem[0]);
        }

        try {
            URI sourceFile = URI.create(sourceURIText);
            URI targetFile = URI.create(sourceURIText.replace("server-test/work", "server-test/clean"));

            String sourceCode = url2Content.get(params.getTextDocument().getUri());

            if (sourceCode == null) {
                sourceCode = readTextContent(sourceFile);
            }

            sourceCode = normalizeLineEndings(sourceCode);

            String targetCode = normalizeLineEndings(readTextContent(targetFile));

            int sourceSuffixIdx = sourceCode.length() - 1;
            int targetSuffixIdx = targetCode.length() - 1;

            while (sourceSuffixIdx > 0 && targetSuffixIdx > 0 && sourceCode.charAt(sourceSuffixIdx) == targetCode.charAt(targetSuffixIdx)) {
                sourceSuffixIdx--;
                targetSuffixIdx--;
            }

            int pos = 0;
            int remainingLines = params.position.getLine();
            int remainingCharacters = params.position.getCharacter();

            while (pos < sourceCode.length()) {
                if (remainingLines == 0) {
                    if (remainingCharacters-- == 0) {
                        break;
                    }
                } else if (sourceCode.charAt(pos) == '\n') {
                    remainingLines--;
                }
                pos++;
            }

            List<InlineCompletionItem> result = new ArrayList<>();

            if (pos < targetSuffixIdx) {
                result.add(new InlineCompletionItem(targetCode.substring(pos, targetSuffixIdx + 1)));
            }

            return CompletableFuture.completedFuture(result.toArray(s -> new InlineCompletionItem[s]));
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    private static String readTextContent(URI uri) throws IOException {
        return Files.readString(new File(uri).toPath());
    }

    private static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n"); //TODO: needed?
    }

    public static class InlineCompletionParams {
        private TextDocumentIdentifier textDocument;
        private Position position;
        private String text; //TODO: remove text
        private InlineCompletionContext context;

        public InlineCompletionParams() {
        }

        public InlineCompletionParams(TextDocumentIdentifier textDocument, Position position, String text, InlineCompletionContext context) {
            this.textDocument = textDocument;
            this.position = position;
            this.text = text;
            this.context = context;
        }

        public TextDocumentIdentifier getTextDocument() {
            return textDocument;
        }

        public void setTextDocument(TextDocumentIdentifier textDocument) {
            this.textDocument = textDocument;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public InlineCompletionContext getContext() {
            return context;
        }

        public void setContext(InlineCompletionContext context) {
            this.context = context;
        }

    }

    public static class InlineCompletionContext {
        private Object triggerKind;
        private Object selectedCompletionInfo;

        public Object getTriggerKind() {
            return triggerKind;
        }

        public void setTriggerKind(Object triggerKind) {
            this.triggerKind = triggerKind;
        }

        public Object getSelectedCompletionInfo() {
            return selectedCompletionInfo;
        }

        public void setSelectedCompletionInfo(Object selectedCompletionInfo) {
            this.selectedCompletionInfo = selectedCompletionInfo;
        }

    }

    public static class InlineCompletionItem {
        private String insertText;

        public InlineCompletionItem(String insertText) {
            this.insertText = insertText;
        }

        public String getInsertText() {
            return insertText;
        }

        public void setInsertText(String insertText) {
            this.insertText = insertText;
        }

    }
}
