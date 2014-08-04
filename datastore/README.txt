README

You can upload an image file (e.g. png, jpg) and it is converted into a string using base64 so we can store it in the datastore and then use it later to send back as json to the android side.

line 113:
        #convert image to string so we store it and use it in json later
        with open(os.path.join(os.path.dirname(__file__), self.request.params["img"].filename), "rb") as imageFile:
            img_string = base64.b64encode(imageFile.read())
            greeting.avatar = img_string

This part: os.path.join(os.path.dirname(__file__) #gets my path in the folder I'm in for this project
This part: self.request.params["img"].filename   #is something like "dachshund.jpg"

The first part will have to be changed to fit the android stuff and get an image from there. We need it give the app engine JSON info like this {"username": "Puppy", "user_id": "1", "date": "8/3/14", "lat": 12, "lon": -123} and an image. 


