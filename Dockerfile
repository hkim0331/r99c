FROM clojure:lein

COPY project.clj /usr/src/app/
WORKDIR /usr/src/app
RUN lein deps

# no use. docker-compose.yml does.
# COPY . /usr/src/app

CMD ["lein", "repl", ":headless"]
