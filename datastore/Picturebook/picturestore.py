import cgi
import urllib
import json
import base64
import os
import webapp2

from google.appengine.api import images
from google.appengine.api import users
from google.appengine.ext import ndb


#creates a FORM which will POST some data to the path "/store"
#content gets submitted as an HTTP POST message to app engine instance
MAIN_PAGE_FOOTER_TEMPLATE = """\
    <form action="/store?%s" enctype="multipart/form-data" method="post">

        <div><textarea name="given_json" rows="3" cols="60"></textarea></div>

        <div><label>Photo:</label></div>
        <div><input type="file" name="img"/></div>

        <div><input type="submit" value="Store Picture Info"></div>
    </form>
    <hr>
    <form>Picturebook name: 
        <input value="%s" name="picturebook_name">
        <input type="submit" value="switch">
    </form>
    <a href="%s">%s</a>
    </body>
</html>
"""

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

        for image in images:
            self.response.write('The json info given from app saved as Image model:')
            self.response.write('<blockquote>image.username: %s</blockquote>' % (image.username))
            self.response.write('<blockquote>image.user_id: %s</blockquote>' % (image.user_id))
            self.response.write('<blockquote>image.date: %s</blockquote>' % (image.date))
            self.response.write('<blockquote>image.photo: %s</blockquote>' % (image.photo))

            self.response.write('____________________________________________________<blockquote></blockquote>')

            image_json = json.dumps([image.to_dict()])
            self.response.write('Info the app requests sent back in json form: ')
            self.response.write('<blockquote>image_json: %s</blockquote>' % (image_json))

        if users.get_current_user():    
            url = users.create_logout_url(self.request.uri)
            url_linktext = 'Logout'
        else:
            url = users.create_login_url(self.request.uri)
            url_linktext = 'Login'

        # Write the submission form and the footer of the page
        store_query_params = urllib.urlencode({'picturebook_name': picturebook_name})
        self.response.write(MAIN_PAGE_FOOTER_TEMPLATE % (store_query_params, cgi.escape(picturebook_name), url, url_linktext))

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
        parsed_json = json.loads(self.request.get('given_json'))  #takes in JSON
        image.username = parsed_json['username']
        image.user_id = parsed_json['user_id']
        image.date = parsed_json['date']

        #convert image to string using base64 so we store it and use it in json later
        with open(os.path.join(os.path.dirname(__file__), self.request.params["img"].filename), "rb") as imageFile:
            img_string = base64.b64encode(imageFile.read())
            image.photo = img_string
        
        #stores image info
        image.put()

        query_params = {'picturebook_name': picturebook_name}
        self.redirect('/?' + urllib.urlencode(query_params))       

application = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/store', PictureStore)  #/sign is the path data is stored to
], debug=True)






