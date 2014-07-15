"""Hello World API implemented using Google Cloud Endpoints.

Defined here are the ProtoRPC messages needed to define Schemas for methods
as well as those methods defined in an API.
"""

import cgi
import urllib
import json
import base64
import os
import webapp2

from google.appengine.api import images
from google.appengine.api import users
from google.appengine.ext import ndb

import endpoints
from protorpc import messages
from protorpc import message_types
from protorpc import remote

package = 'Hello'

DEFAULT_PICTUREBOOK_NAME = 'default_picturebook'

def picturebook_key(picturebook_name=DEFAULT_PICTUREBOOK_NAME):
    """Constructs a Datastore key for a Picturebook entity with picturebook_name."""
    return ndb.Key('Picturebook', picturebook_name)

class Image(ndb.Model):
    """Models an individual Picturebook entry."""
    # sample input json: (will later include location, username of sender, username of sent to)
    username = ndb.StringProperty()
    user_id = ndb.StringProperty()
    date = ndb.StringProperty()
    photo = ndb.TextProperty()

class GivenJson(messages.Message):
    """Greeting that stores a message."""
    message = messages.StringField(1)

@endpoints.api(name='datastore', version='v1')
class DatastoreAPI(remote.Service):
    """Helloworld API v1."""

    ###post

    POST_METHOD_RESOURCE = endpoints.ResourceContainer(GivenJson)

    @endpoints.method(POST_METHOD_RESOURCE, GivenJson, path='hellogreeting', http_method='POST', name='images.post')
    def image_post(self, request):

        #picturebook_name = self.request.get('picturebook_name', DEFAULT_PICTUREBOOK_NAME)
        image = Image(parent=picturebook_key(DEFAULT_PICTUREBOOK_NAME))

        message = request.message
        parsed_json = json.loads(message)  #takes in JSON
        #{"username": "Bob", "user_id": "123", "date": "some date", "photo": "photo_string"}

        image.username = parsed_json['username']
        image.user_id = parsed_json['user_id']
        image.date = parsed_json['date']
        image.photo = parsed_json['photo']
        image.put()

        return GivenJson(message=request.message)

    ###get
    ID_RESOURCE = endpoints.ResourceContainer(message=messages.StringField(1))

    #"{"username": "Bob", "user_id": "123", "date": "some date", "photo": "photo_string"}"
    #"{"username": "Billy", "user_id": "456", "date": "another_date", "photo": "photo_string2"}"
    @endpoints.method(ID_RESOURCE, GivenJson, path='hellogreeting', http_method ='GET', name='images.get')
    def image_get(self, request):
        message = request.message
        parsed_json = json.loads(message)
        images_query = Image.query(ancestor=picturebook_key(DEFAULT_PICTUREBOOK_NAME)).order(-Image.date)

        #need to be able to go through all all the entities...not just the last two..
        #images = images_query.fetch(2)
        images = images_query.fetch(images_query.count())
        for image in images:
            if (image.user_id == parsed_json['user_id']) and (image.photo == parsed_json['photo']):
                return GivenJson(message=image.photo)
        return GivenJson(message='error')


application = endpoints.api_server([DatastoreAPI])





