/* global describe, it */
var assert = require('assert')
var rdf = require('rdf-ext')
var N3Serializer = require('../')

var simpleGraph = rdf.createGraph()

simpleGraph.add(rdf.createTriple(
  rdf.createNamedNode('http://example.org/subject'),
  rdf.createNamedNode('http://example.org/predicate'),
  rdf.createLiteral('object')
))

var simpleGraphNT = '<http://example.org/subject> <http://example.org/predicate> "object".'

// clear prefix map
delete rdf.prefixes.owl
delete rdf.prefixes.rdf
delete rdf.prefixes.rdfa
delete rdf.prefixes.rdfs
delete rdf.prefixes.xhv
delete rdf.prefixes.xsd

describe('N3 serializer', function () {
  describe('instance API', function () {
    describe('callback API', function () {
      it('should be supported', function (done) {
        var serializer = new N3Serializer()

        Promise.resolve(new Promise(function (resolve, reject) {
          serializer.serialize(simpleGraph, function (error, nTriples) {
            if (error) {
              reject(error)
            } else {
              resolve(nTriples)
            }
          })
        })).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleGraphNT)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Promise API', function () {
      it('should be supported', function (done) {
        var serializer = new N3Serializer()

        serializer.serialize(simpleGraph).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleGraphNT)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Stream API', function () {
      it('should be supported', function (done) {
        var serializer = new N3Serializer()
        var nTriples

        serializer.stream(simpleGraph).on('data', function (data) {
          nTriples = data
        }).on('end', function () {
          if (!nTriples) {
            done('no data streamed')
          } else if (nTriples.trim() !== simpleGraphNT) {
            done('wrong output')
          } else {
            done()
          }
        }).on('error', function (error) {
          done(error)
        })
      })
    })
  })

  describe('static API', function () {
    describe('callback API', function () {
      it('should be supported', function (done) {
        Promise.resolve(new Promise(function (resolve, reject) {
          N3Serializer.serialize(simpleGraph, function (error, nTriples) {
            if (error) {
              reject(error)
            } else {
              resolve(nTriples)
            }
          })
        })).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleGraphNT)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Promise API', function () {
      it('should be supported', function (done) {
        N3Serializer.serialize(simpleGraph).then(function (nTriples) {
          assert.equal(nTriples.trim(), simpleGraphNT)

          done()
        }).catch(function (error) {
          done(error)
        })
      })
    })

    describe('Stream API', function () {
      it('should be supported', function (done) {
        var nTriples

        N3Serializer.stream(simpleGraph).on('data', function (data) {
          nTriples = data
        }).on('end', function () {
          if (!nTriples) {
            done('no data streamed')
          } else if (nTriples.trim() !== simpleGraphNT) {
            done('wrong output')
          } else {
            done()
          }
        }).on('error', function (error) {
          done(error)
        })
      })
    })
  })

  describe('example data', function () {
    it('should serializer literal with language', function (done) {
      var graph = rdf.createGraph()
      var triple = rdf.createTriple(
        rdf.createNamedNode('http://example.org/subject'),
        rdf.createNamedNode('http://example.org/predicate'),
        rdf.createLiteral('test', 'en'))

      graph.add(triple)

      N3Serializer.serialize(graph).then(function (nTriples) {
        assert.equal(nTriples.trim(), '<http://example.org/subject> <http://example.org/predicate> "test"@en.')

        done()
      }).catch(function (error) {
        done(error)
      })
    })

    it('should serializer literal with none default datatype', function (done) {
      var graph = rdf.createGraph()
      var triple = rdf.createTriple(
        rdf.createNamedNode('http://example.org/subject'),
        rdf.createNamedNode('http://example.org/predicate'),
        rdf.createLiteral('test', null, rdf.createNamedNode('http://example.org/datatype')))

      graph.add(triple)

      N3Serializer.serialize(graph).then(function (nTriples) {
        assert.equal(nTriples.trim(), '<http://example.org/subject> <http://example.org/predicate> "test"^^<http://example.org/datatype>.')

        done()
      }).catch(function (error) {
        done(error)
      })
    })

    it('should serializer literal with datatype null', function (done) {
      var graph = rdf.createGraph()
      var triple = rdf.createTriple(
        rdf.createNamedNode('http://example.org/subject'),
        rdf.createNamedNode('http://example.org/predicate'),
        rdf.createLiteral('test', null, rdf.createNamedNode('http://example.org/datatype')))

      triple.object.datatype = null

      graph.add(triple)

      N3Serializer.serialize(graph).then(function (nTriples) {
        assert.equal(nTriples.trim(), '<http://example.org/subject> <http://example.org/predicate> "test".')

        done()
      }).catch(function (error) {
        done(error)
      })
    })
  })
})
