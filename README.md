# lein-create-template

A Leiningen plugin for creating templates from existing skeleton projects


##Setup

This plugin requires Leiningen 2.0+

Add to ~/.lein/profiles.clj:

    $ {:user {:plugins [[lein-create-template "0.1.0"]]}}

## Usage

Navigate to the root path of your Leiningen skeleton project and execute

    $ lein create-template <new template name>

The command will create a folder on the root of your Leiningen project.
The folder will be assigned the new template name you provided
and will contain a Leiningen template project of your skeleton project.

Move the template project folder to the destination of choice.
At the root of your new template project execute the following command.

    $ lein install

You can now use your new template by executing

    $ lein new <you new template name> <your new project name>


## License

Distributed under the Eclipse Public License, the same as Clojure.
