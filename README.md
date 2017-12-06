# Tinyauth for Elasticsearch

This is an Elasticsearch plugin for offers authentication and authorization backed by Tinyauth.

To build a dev environment use `docker-compose`:

```
docker-compose build
docker-compose up
```

If you have a java development environment you can build a jar with:

```
gradle assemble
```


## Why?

If you don't already use Tinyauth you should seriously consider using one of the existing options for Elasticsearch:

 * [X-Pack](https://www.elastic.co/guide/en/x-pack/current/security-getting-started.html), the official upstream enterprise option
 * [SearchGuard](https://github.com/floragunncom/search-guard)
 * [readonlyrest-pro](https://readonlyrest.com/pro.html)
