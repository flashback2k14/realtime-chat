# realtime-chat
built with Vert.x 3 and SockJS


- Build the Server with

  ```shell
  ./gradlew stage
  ```

- Run the Server with

  ```shell
  java -jar -Dport=7070 -Ddbname="chats" -Ddburl="mongodb://localhost:27017" build/libs/VertxSocketChat-fat.jar
  ````

- available under
  - DEV: localhost:7070
  - PROD: [https://vertx-realtime-chat.herokuapp.com/](https://vertx-realtime-chat.herokuapp.com/)


inspired by [http://vertx.io/blog/real-time-bidding-with-websockets-and-vert-x/](http://vertx.io/blog/real-time-bidding-with-websockets-and-vert-x/)