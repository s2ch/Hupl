# Hupl
Short for Http UPLoad (imaginative, I know)

Simple Android app, that lets you upload files to servers through the share menu. 
Lets you configure whatever, and how many servers you want, and of course feel free to just use any preconfigured one(s).

Should work with most simple filehosts, that take files via POST. At the very least, it works with ones based on these:
* https://github.com/Rouji/single_php_filehost
* https://github.com/lachs0r/0x0
* https://github.com/ptpb/pb

Support for more complicated http-based uploads, (S)FTP, etc. might follow in future versions. 

Other features:
* Keeps a history of files you upload and resulting links
* Can optionally resize/compress image files before uploading

# Uploaders
Uploader configs are stored, and can be imported, as JSON.
Importing works by either pasting an URL in the appropriate menu, or clicking a web link that points to a .json or .hupl file.

## Http Uploader Json Example
HTTP is currently the only supported uploader type. Here's an example of what a config for that might look like:
```
{
  name: "example uploader",                     //(optional)human-readable name; pulled from the filename, if not given here 
  type: "http",                                 //type of uploader
  targetUrl: "https://example.com/upload.php",  //url to POST to
  fileParam: "myfile",                          //parameter containing your filename
  responseRegex: "<a href=\"(.*)\">",           //(optional)regex to filter the server's response (always uses the first capturing group)
  authUser: "hoge",                             //(optional)http basic auth user
  authPass: "secret"                            //(optional)http basic auth password
}
```

## Default Uploaders
A few preconfigured uploaders get baked into the .apk and imported at first launch of the app. The files for this are in app/src/main/assets/uploaders, which is a git submodule pointing to https://github.com/Rouji/Hupl-Uploaders

# Building
This repo contains an Android Studio (v2) project. You should be able to open it with that and just run/compile from there. 
Note: if you want any preconfigured uploaders, you have to clone the repo recursively.

# Known Issues
Closing the app (e.g. by swiping the main activity off the recent apps screen) sometimes kills any in-progress uploads or cancels notifications, due to them running in the same process. The Android lifecycle stuff is kind of annoying.
