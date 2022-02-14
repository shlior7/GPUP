import express.Express;
import express.ExpressRouter;
import register.WorkerRegister;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class index {

    public static void main(String[] args) throws IOException {
        WorkerRegister workers = new WorkerRegister();


        Express app = new Express() {{
            // Define root greeting
            get("/", (req, res) -> res.send("Hello World!"));
            // Define home routes
            use("/app/worker", workers.router);

            use("/app", new ExpressRouter() {{
                get("/upload", (req, res) -> res.send("About page"));
                get("/graph", (req, res) -> res.send("Impress page"));
                get("/execution", (req, res) -> res.send("Sponsors page"));
            }});

            // Define root routes
            use("/", new ExpressRouter() {{
                get("/login", (req, res) -> res.send("Login page"));
                get("/register", (req, res) -> res.send("Register page"));
                get("/contact", (req, res) -> res.send("Contact page"));
            }});

            // Start server
            System.out.println("Listening on 8080");
            listen(8080);
        }};
    }
}
