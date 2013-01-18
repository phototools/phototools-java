PhotoTools
==========

An application to Manage Photo collections. This includes:
* Reading photos from Mobile Devices to a common, directory structure
* Finding and removing duplicates from your photo collection
* PhotoTools is extensible (through OSGi) so additional functionality can be added via plugins

Background
----------
Many mobile devices can take photos but organizing these photos on a common collection can be a challenge.
Often different tools are to be used to obtain photos from the devices which can result in different ways these photos are
organized on disk resulting in a messy collection.

This application aims to unify this making it possible to create a neat and uniform photo collection with photos and videos
from all your devices.

Getting Started
---------------

Start by downloading the application. The main application build can be [downloaded from here](http://code.google.com/p/coderthoughts/downloads/list).

    unzip PhotoTools_0.5.zip
    sh PhotoTools.sh (on unix or Mac OSX)
    PhotoTools.cmd (on Windows)
    
This will launch the main Application Window:
![Main Window](https://raw.github.com/phototools/phototools/master/docs/images/PhotoTools.png "Main Window")
A number of tabs may be visible. The Photo Copy and About tabs will always be there.


Photo Copy
----------
The Photo Copy tab allows you to copy photos from a device or storage medium to your shared photo collection.

### Sources
The source panel specifies where the photos should be copied from and what photos to copy. A directory source is always 
available. This can be any file system directory, a mounted SD card, a device mounted as a directory or a USB memory stick 
accessible as a directory.

The supported source types are extensible. For example the [phototools-mtp](http://github.com/phototools/phototools-mtp) project
provides a plugin that makes it possible to use MTP-based devices (such as Android phones) as a photo source.
The location in the source panel selects the location from which to copy photos.
Additionally, date ranges and be specified on the source and individual photos can be selected to copy.

The target panel specifies where the photos will be copied to. The target location will be organized using a date-based 
structure. By default a structure that uses years as the top level directories and dates as subdirectories to contain the
photos, for example:

    .../2010/2010-02-14/IMG_2493.JPG
    .../2010/2010-02-14/IMG_2495.JPG
    .../2011/2011-12-24/DSC_1391.JPG
    .../2012/2012-06-09/MOV_0004.MP4
    
The copy button will perform the copy operation. Note the following about the copy operation:
* Photos that are already in the target location will not be copied again. If a photo with the same name is found but it is not identical, a numerical suffix is used to create a different file name.
* Photos are organized on dates based on the date taken if this is available in the photo metadata. 
* If the photo metatada is not available, an attempt is done to find out when the photo was created from the file metadata.
* Only supported photo and video formats will be copied. Other files will be ignored.


Duplicate Finder
----------------

Developer Information
---------------------


More information about the architecture can be found here: http://coderthoughts.blogspot.com/2013/01/a-mobile-device-photo-organizer-using.html
