/**
 * @license Copyright (c) 2003-2014, CKSource - Frederico Knabben, Otavan opisto. All rights reserved.
 */

( function() {
  function isMuikkuTextField( element ) {
    var attributes = element.attributes;

    return (attributes.type == 'application/vnd.muikku.field.text');
  }

  function createFakeParserElement( editor, realElement ) {
    var fake = editor.createFakeParserElement( realElement, 'muikku-text-field', 'muikkutextfield', true );
    fake.attributes.src = "//placehold.it/150x20";
    return fake;
  }

  function createFakeElement( editor, realElement ) {
    var fake = editor.createFakeElement( realElement, 'muikku-text-field', 'muikkutextfield', true );
    fake.setAttribute('src', '//placehold.it/150x20');
    return fake;
  }

  CKEDITOR.plugins.add( 'muikku-textfield', {
    requires: 'dialog,muikku-fields',
    onLoad: function() {
    },
    init: function( editor ) {
      editor.addCommand('muikkutextfield', new CKEDITOR.dialogCommand('muikkutextfield', {
      }));
      editor.ui.addButton && editor.ui.addButton('MuikkuTextField', {
        label: "Muikku text field properties",
        command: 'muikkutextfield',
        toolbar: 'insert,20'
      });
      if (editor.addMenuItems) {
        editor.addMenuItems( {
          muikku: {
            label: editor.lang.flash.properties,
            command: 'muikkutextfield',
            group: 'muikku'
          }
        } );
      }
      editor.on('doubleclick', function(evt) {
        var element = evt.data.element;

        if (element.is( 'img' ) && element.data( 'cke-real-element-type' ) == 'muikkutextfield')
          evt.data.dialog = 'muikkutextfield';
      } );
      if (editor.contextMenu) {
        editor.contextMenu.addListener( function( element, selection ) {
          if ( element && element.is( 'img' ) && !element.isReadOnly() && element.data( 'cke-real-element-type' ) == 'muikkutextfield' )
            return { flash: CKEDITOR.TRISTATE_OFF };
        } );
      }
    },

    afterInit: function(editor) {
      var dataProcessor = editor.dataProcessor,
        dataFilter = dataProcessor && dataProcessor.dataFilter;

      if (dataFilter) {
        dataFilter.addRules( {
          elements: {
            'cke:object': function( element ) {
              if (isMuikkuTextField(element)) {
                return createFakeParserElement( editor, element );
              }
            }
          }
        }, 5);
      }
    }
  } );

  CKEDITOR.dialog.add('muikkutextfield', function (editor) {
    return {
      title : "Muikku Text Field",
      minWidth : 420,
      minHeight : 310,
      onShow : function() {
        var contentJson = editor.getMuikkuFieldDefinition(editor.getSelection().getStartElement());
        this.setupContent(contentJson);
      },
      onOk : function(event) {
        var oldJson = editor.getMuikkuFieldDefinition(editor.getSelection().getStartElement());
        var name;
        if (oldJson.name) {
          name = oldJson.name;
        } else {
          name = editor.createRandomMuikkuFieldName();
        }
        var rightAnswers = [];
        var formAnswers = this.getContentElement('info', 'answers').getValue().split('\n');
        for (var i=0, l=formAnswers.length; i<l; i++) {
          var formAnswer = formAnswers[i];
          rightAnswers.push({
            // TODO
            'points': 1.0,
            'text': formAnswer,
            'caseSensitive': false,
            'normalizeWhitespace': true
          });
        }
        var newJson = {
          'name': name,
          'rightAnswers': rightAnswers,
          'columns': this.getContentElement('info', 'width').getValue(),
          'hint': this.getContentElement('info', 'hint').getValue(),
          'help': this.getContentElement('info', 'help').getValue()
        };
        
        var object = new CKEDITOR.dom.element('object');
        object.setAttribute('type', 'application/vnd.muikku.field.text');
        var paramType = new CKEDITOR.dom.element('param');
        paramType.setAttribute('name', 'type');
        paramType.setAttribute('value', 'application/json');
        var paramContent = new CKEDITOR.dom.element('param');
        paramContent.setAttribute('name', 'content');
        paramContent.setAttribute('value', JSON.stringify(newJson));
        object.append(paramType);
        object.append(paramContent);
        var fakeElement = createFakeElement(editor, object);
        editor.insertElement(fakeElement);
      },
      onHide : function() {
      },
      contents : [ {
        id : 'info',
        label : editor.lang.common.generalTab,
        elements : [
          {
            id: 'width',
            type: 'text',
            label: 'Width',
            setup: function(json) {
              this.setValue(json.columns);
            }
          },
          {
            id: 'hint',
            type: 'text',
            label: 'Hint',
            setup: function(json) {
              this.setValue(json.hint);
            }
          },
          {
            id: 'help',
            type: 'text',
            label: 'Help',
            setup: function(json) {
              this.setValue(json.help);
            }
          },
          {
            id: 'answers',
            type: 'textarea',
            label: 'Answers'
          }
        ]
      } ]
    };
  });

} )();