# GetNZB #
GetNZB is a Android client for searching .nzb files on http://www.nzbs.org and uploading the files to a HellaNZB server with XMLRPC or an FTP server. It is a utility meant as a front-end for http://www.nzbs.org and remotely adding .nzb files to a newsgrabber, which then automatically starts downloading the wanted files from Usenet. Most newsgrabbers support monitoring a specific directory for new .nzb files. There are a lot of free Windows (and `*`nix) FTP servers available, so setting one up and using GetNZB gives you the possibility to download whenever you want and have the whole thing available when you get home!

GetNZB is available on this site for download, or you can look it up in Android Market!


## ToDo ##
In no particular order:

  * implementing functionality to download multiple .nzb files (in .zip format, on http://www.nzbs.org this is the 'get Cart ' function).
  * Monitor various other newsgrabber (SABnzbd+ etc.)
  * Upload .nzb files to Dropbox account.

## Version ##
Current version is **v0.66**.
  * Manipulation of HellaNZB queue.
  * Ability to use custom saved searches (My Searches)
  * Monitoring HellaNZB.
  * Uploading files to FTP server.
  * Searching on http://www.nzbs.org , within categories.
  * downloading .nzb files to local storage.
  * Uploading .nzb files to a HellaNZB server utilizing XMLRPC.
  * Manage local stored files.
  * .nzb file metadata (size, age, category) in search and local list.

### Changelog ###
**v0.66** - FTP Upload check. Bugfixes (Force close on no search results fixed).

**v0.65** - Speed improvements in search and mysearch. Several bugfixes.

**v0.6** - Added ability to manipulate HellaNZB queue and use custom searches (My Searches). Adjustable HellaNZB refresh timer.

**v0.5** - Added ability to monitor HellaNZB.

**v0.4** - UI Improvements. Added ability to upload .nzb files to FTP server.

**v0.3** - UI Improvements. Bugfixes. Searching in Category. .nzb Metadata (age, size, category) in local and search list.

**v0.2** - UI Improvements. Bugfixes. Removing locally stored .nzb files without having to upload them to HellaNZB.

**v0.1** - Basic functionality.

**v0.1-beta** - Initial Release.


## Requirements ##
  * Android 2.1 (maybe 2.2 and 1.6 will work, but not tested... please give feedback if you try this!)
  * Account on http://www.nzbs.org
  * Access to a HellaNZB server to upload .nzb files to. (it should be accesible from outside your firewall, standard port is 8760, but any port will do and can be set in the GetNZB preferences)
  * Or an FTP server to upload the .nzb files to. So your favorite news-grabber can automatically import new .nzb files.

## Screenshots ##

Searching http://www.nzbs.org:

<img src='http://www.rldsoftware.nl/images/getnzb/mainscreen.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/search-4.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/search-2.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/search-3.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/dowloading-nzb-file.png' />

My Searches:

<img src='http://www.rldsoftware.nl/images/getnzb/my-searches-1.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/add-my-search-1.png' />

Locally stored files:

<img src='http://www.rldsoftware.nl/images/getnzb/localfiles-1.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/localfiles-context.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/localfiles-uploadftp.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/localfiles-uploadhellanzb.png' />

Monitoring Newsgrabber:

<img src='http://www.rldsoftware.nl/images/getnzb/monitor-1.png' />
<img src='http://www.rldsoftware.nl/images/getnzb/monitor-manipulate-queue-1.png' />