# Portfolio Website

## Static, Re-Frame based SPA

This portfolio is designed to be a cheap yet fluid and interactive portfolio website.
It is written in Clojurescript and re-frame.

## Posts
Projects have writeups in the form of blog posts, which are written in markdown. Each post lives in its own markdown file, with a description in posts.edn. Posts can also have their own resources, such as images, CSVs (converted to tables) or even interactive elements (CAD renderings, annotated schematic diagrams, etc).

## App DB
The app db lives in a typical re-frame app db and has the following "schema":
- posts
- cur-post
- post-slugs
- error
- loading-posts?

### posts
`posts` contains a map of post contents, with the keys being the string of the post name (read in from the posts.edn file)

### cur-post
`cur-post` is the currently selected post key (string)

### post-slugs
This is the list of post keys

### error
This is the current error that displays on the page

### loading-posts?
This is the boolean that states whether posts exist in the db yet
