class Groundwork < Formula
  desc "CLI for bootstrapping new projects from recurring local code patterns"
  homepage "https://github.com/zkr99/groundwork"
  url "https://github.com/zkr99/groundwork/releases/download/v0.1.0-alpha.1/groundwork-0.1.0-alpha.1.tar"
  sha256 "6670dba2ac8da73d4ec3a8fe3c280f3cc2880437bc232c037947b0559a472c87"
  license "MIT"

  depends_on "openjdk"

  def install
    libexec.install Dir["*"]

    Pathname.glob("#{libexec}/bin/*") do |file|
      next if file.directory?
      next if file.extname == ".bat"

      (bin/file.basename).write_env_script file, Language::Java.overridable_java_home_env
    end
  end

  test do
    assert_match "Groundwork 0.1.0-alpha.1", shell_output("#{bin}/groundwork --version")
  end
end
