# lein-create-template

A Leiningen plugin for creating templates from existing skeleton projects

## Usage

In alpha state!!

Under construction, not yet on clojars!

Put `[lein-create-template "0.1.0"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-create-template 0.1.0`.


    $ lein create-template <new template name>


Will create a folder on the root of your Leiningen project.
The folder will be assigned the name you provided
and will contain a Leiningen template project of your skeleton project.


## License

Distributed under the Eclipse Public License, the same as Clojure.
