(function propertiesbuilder(attr) {
    return {
      user: attr[connectionHelper.attributeUsername],
      password: attr[connectionHelper.attributePassword]
    };
})
