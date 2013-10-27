eDAB
====

[Try the current version of eDAB](https://edab-ds.appspot.com)

One day, eDAB hopes to be a fully fledged replacement for traditional assignment notebooks.
It aims to provide a synchronized, cross-platform interface to input and manage homework.
Also, people in the same class will be able to share assignments, upcoming tests, and notes, in order to effectively crowdsource information.

Currently, eDAB requires users to authorize via an [fcpsschools.net](http://fcpsschools.net) account - which means that only [Fairfax County Public Schools](http://www.fcps.edu) will be able to use it.
This is mainly because I'm an FCPS student, and federated login is a quick and easy method to ensure that everyone can log in and validate their identity.
Obviously, it would be nice if the system was expanded in the future to allow for other school systems, or simply signing in with social media accounts, but that would involve more complexity.

Right now, eDAB is being developed as a web app, hopefully responsive to also work on mobile devices.
Further expansion is certainly expected after eDAB actually starts doing something, including a native Android app.
But hopefully, the web app will be good enough so that won't be completely necessary.

![An early mock-up of eDAB from January 2013](https://docs.google.com/drawings/d/1nV0ClG9MEMLFkj039nbWbxTTDDp9VwdwuN1XO-Abl7A/pub?w=1499&amp;h=1097)
An early mock-up of eDAB from January 2013

## Technical Overview / Credits ##
eDAB, on the client side, is a [single-page application](http://en.wikipedia.org/wiki/Single-page_application), thanks to the help of [Angular.js](http://angularjs.org). This means it never reloads; it retrieves all data via Ajax (which is really a terribly overused buzzword). It also uses [Bootstrap](http://getbootstrap.com) because I'm not that pro at designing. The server-side components were built using [Google App Engine](https://appengine.google.com). It manages database interactions with [Objectify](https://code.google.com/p/objectify-appengine/), and the data is marshalled into JSON with the help of [Jackson](https://github.com/FasterXML/jackson). Finally, it's transferred to the client via a REST-like API powered by [Jersey](http://jersey.java.net).

## Contributing ##

If you are interested in contributing, please [contact me](https://plus.google.com/112830849462286532136/about)!
Even if you don't know how to use App Engine or Angular.js or any of this, if you're willing to learn or help with anything, including graphics or logo design, promotion, documentation, background photography, please let me know! :)

### Config.java ###

Before building, you'll need a `Config.java` file, which contains several configuration details outlined in `Config.java.template`.
You'll also need a [Google APIs Console](https://code.google.com/apis/console/) project.
If you want to contribute, please contact me and I'll give you the proper details for the ones I'm using right now. 

Otherwise, if you want to clone eDAB or something, you can create your own configuration file from the template.
If you're planning on deploying to App Engine, creating a new App Engine application will also create a Google APIs Console project that you can get a client ID/secret from.
And don't forget to change the application id... :c

### Building ###

I've been developing eDAB from Eclipse.
So the easiest way to get started is simply to use Eclipse and the [App Engine SDK for Eclipse](https://developers.google.com/appengine/docs/java/tools/eclipse).
Check out eDAB from git, and File -> Import the project.
From there you can run the development server and work on the project.

If you want to use another IDE or something, make sure you have the App Engine SDK, and include all of the files in `/war/WEB-INF/lib` in the project's classpath.
I probably can't really help you...

## Contact us ##

eDAB is a product of [Desklamp Studios](https://plus.google.com/113383006901215901732), which was formed when [Richard](https://plus.google.com/110382605130903206834), [Jenny](https://plus.google.com/107757960491028812136), and I ([Kevin](https://plus.google.com/112830849462286532136)) were bored and we wanted to do something in early 2013.
We spent a good amount of time planning and making the mock-up shown above together. 
Since then, I've gotten some baseline code done, which is basically all the project is right now.
Contact Desklamp Studios on Google+ if you have any questions!

Later, we may also be looking for aspiring photographers to provide background images for eDAB. :)