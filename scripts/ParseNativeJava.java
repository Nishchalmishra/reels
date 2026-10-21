import javax.tools.*;
import com.sun.source.util.JavacTask;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/** Fast compiler syntax check while Gradle resolves the full Android classpath.
 * This is deliberately not a replacement for compilation or device verification. */
class ParseNativeJava {
    public static void main(String[] args) throws Exception {
        var compiler=ToolProvider.getSystemJavaCompiler();
        var diagnostics=new DiagnosticCollector<JavaFileObject>();
        try(var manager=compiler.getStandardFileManager(diagnostics,Locale.ROOT,StandardCharsets.UTF_8);
            var paths=Files.walk(Path.of(args[0]))) {
            var files=paths.filter(p->p.toString().endsWith(".java")).map(Path::toFile).toList();
            var task=(JavacTask)compiler.getTask(null,manager,diagnostics,List.of("-proc:none","--release","17"),null,manager.getJavaFileObjectsFromFiles(files));
            task.parse();
            boolean failed=false;
            for(var d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR){
                failed=true;System.err.println(d.getSource().getName()+":"+d.getLineNumber()+" "+d.getMessage(Locale.ROOT));
            }
            if(failed)System.exit(1);
            System.out.println("PASS Java compiler syntax parse: "+files.size()+" native source files. Android compilation remains a separate check.");
        }
    }
}
