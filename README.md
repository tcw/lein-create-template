# lein-create-template

A Leiningen plugin for creating templates from existing skeleton projects


##Setup

This plugin requires Leiningen 2.0+

Add to ~/.lein/profiles.clj:

    {:user {:plugins [[lein-create-template "0.2.0"]]}}

## Usage

Navigate to the root path of your Leiningen skeleton project and execute

    $ lein create-template <new template name>

The command will create a folder on the root of your Leiningen project.
The folder will be assigned the new template name you provided
and will contain a Leiningen template project of your skeleton project.

(NOTE: Make sure that the directories of your skeleton project are
clean of any extraneous files (e.g. editor temp files). Otherwise,
these files will be dragged along into your template. Also, note that
many common tools hide .gitignore'd files and make it easy to overlook
some of these extra files. So, it is a good idea to build the template
from a clean clone of the skeleton, or carefully remove all
temp/output files.)

The template will automatically include the source files, test files,
and project.clj of your skeleton project. If you want to include other
files too, you can specify them by adding a :template-additions clause
to your project.clj, e.g.:

    :template-additions [".gitignore" "README.md"]


Move the template project folder to the destination of choice.
At the root of your new template project execute the following command.

    $ lein install

You can now use your new template by executing

    $ lein new <you new template name> <your new project name>


WARNING: If your project is a *-SNAPSHOT version, you may get the
error 'Failed to resolve version for :lein-template:jar:RELEASE'.  A
workaround for this problem seems to be to run the 'lein new' command
while within the directory of the template project. I don't understand
why this works; see the discussion referenced in [this
issue](https://github.com/tcw/lein-create-template/issues/2).


## License

Distributed under the Eclipse Public License, the same as Clojure.
