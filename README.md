# compass - Backend for managing Short-Links

How to run
----------
Use this docker-compose.yml

<pre><code>compass:
  image: imasen/compass
  ports:
    - "8080:8080"
  environment:
    REDIS: redis
  links:
    - redis
  restart: always

redis:
  image: redis
  restart: always</code></pre>

## Create Shortlink
<pre><code>curl -X POST http://localhost:8080/rest/1.0/shortlink/create?dest=http://example.com</code></pre>

## Delete Shortlink
<pre><code>curl -X DELETE http://localhost:8080/rest/1.0/shortlink/delete?id=a9b9f043</code></pre>

## Get all Shortlinks
<pre><code>curl -X GET http://localhost:8080/rest/1.0/shortlink/getall</code></pre>
