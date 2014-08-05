import cgi
import urllib
import json
import base64
import os

from google.appengine.api import images
from google.appengine.api import users
from google.appengine.ext import ndb

import webapp2
import jinja2

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

DEFAULT_PICTUREBOOK_NAME = 'default_picturebook'

# We set a parent key on the 'Images' to ensure that they are all in the same
# entity group. Queries across the single entity group will be consistent.
# However, the write rate should be limited to ~1/second.
def picturebook_key(picturebook_name=DEFAULT_PICTUREBOOK_NAME):
    """Constructs a Datastore key for a Picturebook entity with picturebook_name."""
    return ndb.Key('Picturebook', picturebook_name)

#Image properties
class Image(ndb.Model):
    """Models an individual Picturebook entry."""
    # sample input json: (will later include location, username of sender, username of sent to)
    #{"username": "Bob", "user_id": "123", "date": "some date"}
    username = ndb.StringProperty()
    user_id = ndb.StringProperty()
    date = ndb.StringProperty()
    photo = ndb.TextProperty()
    lat = ndb.StringProperty()
    lon = ndb.StringProperty()

class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write('<html><body>')
        picturebook_name = self.request.get('picturebook_name',
                                          DEFAULT_PICTUREBOOK_NAME)

        # Ancestor Queries, as shown here, are strongly consistent with the High
        # Replication Datastore. Queries that span entity groups are eventually
        # consistent. If we omitted the ancestor from this query there would be
        # a slight chance that Image that had just been written would not
        # show up in a query.
      
        #query is a function Image inherits from ndb.Model
        #following returns last 3 image stores
        images_query = Image.query(
            ancestor=picturebook_key(picturebook_name)).order(-Image.date)
        images = images_query.fetch(3)

        if users.get_current_user():    
            url = users.create_logout_url(self.request.uri)
            url_linktext = 'Logout'
        else:
            url = users.create_login_url(self.request.uri)
            url_linktext = 'Login'

        template_values = {
            'images': images,
            'picturebook_name': urllib.quote_plus(picturebook_name),
            'url': url,
            'url_linktext': url_linktext,
        }

        # Write the submission form and the footer of the page
        store_query_params = urllib.urlencode({'picturebook_name': picturebook_name})
        template = JINJA_ENVIRONMENT.get_template('index.html')
        self.response.write(template.render(template_values))

#lets us modify data store with the content that we got
class PictureStore(webapp2.RequestHandler):
    def post(self):
        # We set the same parent key on the 'Image' to ensure each Image
        # is in the same entity group. Queries across the single entity group
        # will be consistent. However, the write rate to a single entity group
        # should be limited to ~1/second.
        picturebook_name = self.request.get('picturebook_name', DEFAULT_PICTUREBOOK_NAME)
        image = Image(parent=picturebook_key(picturebook_name))

        #get json input
        #parsed_json = json.loads(self.request.get('given_json'))  #takes in JSON
        image.username = self.request.get('username')
        image.user_id = self.request.get('user_id')
        image.date = self.request.get('date')
        image.lat = self.request.get('lat')
        image.lon = self.request.get('lon')
        image.photo = self.request.get('photo')

        #convert image to string using base64 so we store it and use it in json later
    #    with open(os.path.join(os.path.dirname(__file__), self.request.params["img"].filename), "rb") as imageFile:
    #        img_string = base64.b64encode(imageFile.read())
    #        image.photo = img_string
        
        #stores image info
        image.put()

        query_params = {'picturebook_name': picturebook_name}
        self.redirect('/?' + urllib.urlencode(query_params))       

application = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/store', PictureStore)  #/sign is the path data is stored to
], debug=True)







