# Pocket Sermons

Open Source Sermon video Android Application is designed for educational purposes of building a [REST-ful API](https://github.com/tmack8001/pocketsermons-server) and an accompanying multi platform Android application

[![Build Status](https://travis-ci.org/tmack8001/PocketSermons.svg?branch=master)](https://travis-ci.org/tmack8001/PocketSermons)


## Download on Google Play

<a href="https://play.google.com/store/apps/details?id=com.tmack.sermonstream">
<img alt="Get it on Google Play" src="http://developer.android.com/images/brand/en_generic_rgb_wo_45.png" />
</a>

## Delivery Instructions to Google Play

1. Verify all new features are complete and tested.
2. Update all play store files to upload via ```./gradlew mobile:publishListingRelease```
3. Upload alpha APK for final testing of :mobile ```./gradlew mobile:publishApkRelease```
4. Up the code revision number in ```./gradle.properties``` in preparation of :tv release
5. Upload alpha APK for final testing of :tv ```./gradlew tv:publishApkRelease```

## Change List
0.2.3
  * Updated to [CastCompanionLibrary v2.4](https://github.com/googlecast/CastCompanionLibrary-android#change-list) fixing a bunch of various underlying issues 
  * Adding RecyclerView usage as per [#1](https://github.com/tmack8001/PocketSermons/issues/5)
  * Provided ground work for a [Scrollable Reloading List](https://github.com/tmack8001/PocketSermons/issues/1)
  * Clean up some use of deprecated code as per [#4](https://github.com/tmack8001/PocketSermons/issues/4)

0.2.2
  * Clean up usage of Android Support Library
  * Make use of Android's Recycler View
  * Collapsible Toolbar making it easier to scroll through content

0.2.1
  * Adapting to the CCL v2.1
  * Fixing some typos and minor issues
  * Adding incremental volume step control
  * Refactored code into 4 modules (:api, :common, :mobile, :tv) for better granularity of features and reuse of common code
  * Updated to all for Continuous Delivery for Google Play Alpha channel with the [gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher)
  * Merged both Phone/Tablet application and Android Tv applications into a single [listing](https://play.google.com/store/apps/details?id=com.tmack.sermonstream) with multiple active APKs
  * Fixed few bugs in previous releases

0.2.0
  * First Google Play release version
  * Added a TV module using LeanbackTV libraries
  * Update to CCL v1.14