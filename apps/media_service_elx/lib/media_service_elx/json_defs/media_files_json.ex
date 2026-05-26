defmodule MediaServiceElxWeb.MediaFilesJSON do
  @moduledoc """
  This file handles how the data is shown to the user
  e.g. what fields are shows when an database entry is returned from a request
  """

  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaInstances.MediaInstance

  def instance_index(%{instances: instances}) do
    %{data: for(instance <- instances, do: instance_data(instance))}
  end

  def instance_show(%{instance: instance}) do
    %{data: instance_data(instance)}
  end

  defp instance_data(%MediaInstance{} = instance) do
    %{
      id: instance.id
    }
  end

  def partial_file_index(%{files: files}) do
    %{data: for(file <- files, do: partial_file_data(file))}
  end

  def full_file_index(%{files: files}) do
    %{data: for(file <- files, do: full_file_data(file))}
  end

  def partial_file_show(%{file: file}) do
    %{data: partial_file_data(file)}
  end

  def full_file_show(%{file: file}) do
    %{data: full_file_data(file)}
  end

  defp partial_file_data(%MediaFile{} = file) do
    %{
      contentFlag: file.content_flag,
      fileData: Base.encode64(file.original),
      contentType: file.content_type
    }
  end

  defp full_file_data(%MediaFile{} = file) do
    %{
      id: file.id,
      content_flag: file.content_flag,
      file: Base.encode64(file.original),
      content_type: file.content_type
    }
  end
end