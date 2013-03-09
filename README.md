Project October Backend
=======================
[![Build Status](https://api.travis-ci.org/bis12/project-october-backend.png)](https://travis-ci.org/bis12/project-october-backend)

This project maintains a graph of users, posts, and comments for [Project October](https://github.com/ted27/project-october).

The three main components of Project October are:

* Frontend https://github.com/ted27/project-october
* Backend https://github.com/bis12/project-october-backend
* Thrift API https://github.com/bis12/project-october-api

View the frontend project for more detailed information on Project October.

Installing
----------
This project requires [Scala Build Tool](http://www.scala-sbt.org/) to run.  Install it and clone this project.  You should be good to go.

Running
-------
Optionally set the `OCTOBER_ENV` environment variable.  The options are

* Development -- This is the standard env
* Production -- Use this for running in production
* Test -- You most likely should not need to use this directly
* Remote -- Use this to run the development server for the Rails server to use

Now, simply execute `sbt run` from the top level directory.

Testing
-------
Simply execute `sbt test` from the top level directory.

Authors
-------
* [Mika Little](http://letsgetmikaawebsite.com)
* [Tom Dooner](http://tomdooner.com)
* [Brian Stack](http://brianstack.net)
* [Raja Cherukuri](http://rxc178.github.com/)
