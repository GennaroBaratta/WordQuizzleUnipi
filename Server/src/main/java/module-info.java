module Server {
    requires java.rmi;
    requires Shared;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    opens server to com.fasterxml.jackson.databind;
    exports server;
}