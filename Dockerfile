FROM clojure:openjdk-18-lein-2.9.8-slim-bullseye

COPY project.clj /usr/src/app/
WORKDIR /usr/src/app
RUN lein deps
COPY . /usr/src/app

CMD ["lein", "repl", ":headless"]
