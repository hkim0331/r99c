FROM clojure:openjdk-17-lein-2.9.8-bullseye

COPY project.clj /usr/src/app/
WORKDIR /usr/src/app
RUN lein deps
COPY . /usr/src/app

CMD ["lein", "repl", ":headless"]
