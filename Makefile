.phony: run-tests install clean

run-tests:
	clojure -T:build test

target:
	clojure -T:build ci

install: target
	clojure -T:build install

clean:
	rm -rf target
