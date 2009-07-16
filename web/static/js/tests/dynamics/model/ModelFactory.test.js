
$(document).ready(function() {
  
  module("Dynamics: Model factory",{
    setup: function() {
      ModelFactory.instance = null;
      this.instance = ModelFactory.getInstance();
      this.testObject = {
          id: 222,
          name: "Test Object"
      };
    },  
    teardown: function() { }
  });
  
  
  test("Get instance", function() {
    ModelFactory.instance = null;
    var instance = ModelFactory.getInstance();
    
    ok(ModelFactory.instance, "Instance has been created");
    
    var anotherInstance = ModelFactory.getInstance();
    equals(anotherInstance, instance, "Instance is singleton");
  });
  
  
  test("Static get object", function() {
    var me = this;
    var expectedType = "object";
    var expectedId = 222;
    
    var internalGetObjectCallCount = 0;
   
    this.instance._getObject = function(type, id) {
      internalGetObjectCallCount++;
      same(type, expectedType, "Type matches");
      same(id, expectedId, "Id matches");
      return me.testObject; 
    };
    
    equals(ModelFactory.getObject(expectedType, expectedId), this.testObject, "Correct object returned");
    same(internalGetObjectCallCount, 1, "Internal getObject function called");
  });
  
  test("Static create object", function() {
    var expectedType = "object";
    var newObject = {};
    
    var internalCreateObjectCallCount = 0;
    this.instance._createObject = function(type) {
      same(type, expectedType, "Type matches");     
      internalCreateObjectCallCount++;
      return newObject;
    };
    
    equals(ModelFactory.createObject(expectedType), newObject, "Correct object returned");
    same(internalCreateObjectCallCount, 1, "Internal createObject function called");
  });
  
  
  test("Internal get object", function() {
    this.instance.data = {
      story: {
        123: {
          id: 123,
          name: "Test story with id 123"
        }
      },
      task: {
        123: {
          id: 123,
          name: "Test task with id 123"
        },
        7: {
          id: 7,
          name: "Test task with id 7"
        }
      }
    };
    
    var task123 = this.instance._getObject(ModelFactory.types.task, 123);
    var task7 = this.instance._getObject(ModelFactory.types.task, 7);
    var story123 = this.instance._getObject(ModelFactory.types.story, 123);
    
    var notFoundStory = this.instance._getObject(ModelFactory.types.story, 9876);

    ok(task123, "Task 123 is defined");
    ok(task7, "Task 7 is defined");
    ok(story123, "Story 123 is defined");
    
    equals(task123, this.instance.data.task[123], "Task with id 123 is returned");
    equals(task7, this.instance.data.task[7], "Task with id 7 is returned");    
    equals(story123, this.instance.data.story[123], "Story with id 123 is returned");
    
    equals(notFoundStory, null, "Story with id 123 is returned");
  });

  
  test("Internal create get null checks", function() {    
    // Undefined
    var exceptionCount = 0;
    try {
      this.instance._getObject();
    }
    catch (e) {
      exceptionCount++;
    }
    
    // Null
    try {
      this.instance._getObject(null);
    }
    catch (e) {
      exceptionCount++;
    }
    
    // Invalid
    try {
      this.instance._getObject("This is invalid");
    }
    catch (e) {
      exceptionCount++;
    }
    
    same(exceptionCount, 3, "Correct number of exceptions thrown");
  });
  
  
  
  test("Internal create object", function() {
    ok(this.instance._createObject(ModelFactory.types.task) instanceof TaskModel,
      "Task created correctly");
    ok(this.instance._createObject(ModelFactory.types.story) instanceof StoryModel,
      "Story created correctly");
  });
  
  test("Internal create object null checks", function() {    
    // Undefined
    var exceptionCount = 0;
    try {
      this.instance._createObject();
    }
    catch (e) {
      exceptionCount++;
    }
    
    // Null
    try {
      this.instance._createObject(null);
    }
    catch (e) {
      exceptionCount++;
    }
    
    // Invalid
    try {
      this.instance._createObject("This is invalid");
    }
    catch (e) {
      exceptionCount++;
    }
    
    same(exceptionCount, 3, "Correct number of exceptions thrown");
  });
});

