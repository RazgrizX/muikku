for FILE in index.frontpage.jsx index.jsx
	do browserify -o "dist/${FILE::(-1)}" -t [ babelify --presets [ env react ] --plugins [ transform-class-properties ] ] $FILE -d
done
