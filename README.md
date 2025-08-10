A language server providing inline completion for testing (of NetBeans)
==========

There's a new proposed `inlineCompletion` even for the Language Server Protocol (LSP).

There's a sketch of NetBeans support here: https://github.com/jlahoda/netbeans/tree/lsp.client.inline.completion

The code here should help testing that:
- checkout and build the branch above.
- open `ide/nb` from this repository in NetBeans, set the NetBeans platform to the NetBeans binary built above
- `Run` the project
- have a directory called `server-test`, with two sub-directories, `clean` and `work`. These should contain the same files. Open a file from the `work` sub-directory, and delete part of the file. The server will inject the corresponding content from the corresponding file in the `clean` directory.
