Tasks:
- Update getIgnoredFiles to 
- Bugs to fix:
    - master branch not created when initializing repository (UnitTest.java) 
      - issue: using incorrect IO function when reading branch
      - solution: use Utils.readContentsAsString function
    - .gitlet folder unable to be deleted since index.txt is not deleted
      - issue: using incorrect IO function to read HEAD commit identifier
      - solution: use Utils.readContentsAsString function